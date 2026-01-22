package com.smplatform.product_service.domain.order.repository.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.smplatform.product_service.domain.order.dto.AdminOrderSearchRequestDto;
import com.smplatform.product_service.domain.order.dto.OrderSearchRequestDto;
import com.smplatform.product_service.domain.order.dto.OrderSearchResponseDto;
import com.smplatform.product_service.domain.order.dto.QOrderSearchResponseDto_AdminOrder;
import com.smplatform.product_service.domain.order.dto.QOrderSearchResponseDto_MemberOrder;
import com.smplatform.product_service.domain.order.entity.OrderProduct;
import com.smplatform.product_service.domain.order.entity.OrderProductStatus;
import com.smplatform.product_service.domain.order.entity.OrderStatus;
import com.smplatform.product_service.domain.order.entity.QOrder;
import com.smplatform.product_service.domain.order.entity.QOrderProduct;
import com.smplatform.product_service.domain.order.repository.CustomOrderSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
public class CustomOrderSearchRepositoryImpl implements CustomOrderSearchRepository {
    private final JPAQueryFactory query;
    private final QOrder order = QOrder.order;
    private final QOrderProduct orderProduct = QOrderProduct.orderProduct;

    @Override
    public Page<OrderSearchResponseDto.MemberOrder> findAllMemberOrderBy(String memberId, OrderSearchRequestDto.MemberOrdersSearch request, Pageable pageable) {

        // where 조건
        BooleanBuilder conditions = new BooleanBuilder()
                .and(order.member.memberId.eq(memberId))
                .and(dateRange(request.getConditions()))
                .and(typeFilter(request.getConditions().getType()))
                .and(statusFilter(request.getConditions().getStatus()));

        // response의 "content" 값
        List<OrderSearchResponseDto.MemberOrder> content = query
                .select(new QOrderSearchResponseDto_MemberOrder(
                        order.orderId,
                        order.orderDate,
                        order.orderTitle,
                        orderProduct.orderProductId.count().intValue(),
                        order.totalPrice)
                )
                .from(order)
                .join(orderProduct).on(order.eq(orderProduct.order))
                .where(conditions)
                .groupBy(order.orderId)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = query
                .select(order.orderId.countDistinct())
                .from(order)
                .join(orderProduct).on(orderProduct.order.eq(order))
                .where(conditions)
                .fetchOne();

        return PageableExecutionUtils.getPage(content, pageable, () -> total);
    }

    @Override
    public List<OrderProduct> findOrderProductsByOrderIds(List<Long> orderIds) {
        return query
                .selectFrom(orderProduct)
                .where(orderProduct.order.orderId.in(orderIds))
                .fetch();
    }

    @Override
    public Page<OrderSearchResponseDto.AdminOrder> findAllAdminOrderBy(AdminOrderSearchRequestDto.AdminOrdersSearch request, Pageable pageable) {
        AdminOrderSearchRequestDto.SearchConditionsDto conditions = request.getConditions();
        
        // where 조건
        BooleanBuilder whereConditions = new BooleanBuilder();
        
        if (conditions.getOrderId() != null) {
            whereConditions.and(order.orderId.eq(conditions.getOrderId()));
        }
        if (conditions.getOrderMemberId() != null && !conditions.getOrderMemberId().isEmpty()) {
            whereConditions.and(order.member.memberId.contains(conditions.getOrderMemberId()));
        }
        if (conditions.getOrderMemberName() != null && !conditions.getOrderMemberName().isEmpty()) {
            whereConditions.and(order.member.name.contains(conditions.getOrderMemberName()));
        }
        if (conditions.getOrderProductName() != null && !conditions.getOrderProductName().isEmpty()) {
            whereConditions.and(orderProduct.productName.contains(conditions.getOrderProductName()));
        }
        if (conditions.getStartDate() != null && conditions.getEndDate() != null) {
            whereConditions.and(order.orderDate.between(
                    conditions.getStartDate().atStartOfDay(),
                    conditions.getEndDate().plusDays(1).atStartOfDay()
            ));
        }
        if (conditions.getType() != null && !AdminOrderSearchRequestDto.OrderSearchType.ALL.equals(conditions.getType())) {
            whereConditions.and(adminTypeFilter(conditions.getType()));
        }
        if (conditions.getStatus() != null && !conditions.getStatus().isEmpty() && !"전체".equals(conditions.getStatus())) {
            whereConditions.and(adminStatusFilter(conditions.getStatus()));
        }

        // response의 "content" 값
        List<OrderSearchResponseDto.AdminOrder> content = query
                .select(new QOrderSearchResponseDto_AdminOrder(
                        order.orderId,
                        order.orderDate,
                        order.member.name,
                        order.orderTitle,
                        order.totalPrice,
                        order.totalPrice,  // paymentPrice (임시로 totalPrice 사용)
                        order.orderNumber.stringValue(),  // paymentMethod (임시)
                        order.orderStatus.stringValue()  // orderStatus (String 값)
                )
                )
                .from(order)
                .leftJoin(orderProduct).on(order.eq(orderProduct.order))
                .where(whereConditions)
                .groupBy(order.orderId)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch()
                .stream()
                .peek(adminOrder -> adminOrder.setOrderStatus(getOrderStatusLabel(adminOrder.getOrderStatus())))
                .toList();

        Long total = query
                .select(order.orderId.countDistinct())
                .from(order)
                .leftJoin(orderProduct).on(orderProduct.order.eq(order))
                .where(whereConditions)
                .fetchOne();

        return PageableExecutionUtils.getPage(content, pageable, () -> total == null ? 0 : total);
    }

    private BooleanExpression dateRange(OrderSearchRequestDto.SearchConditionsDto conditions) {
        return order.orderDate.between(
                conditions.getStartDate().atStartOfDay(),
                conditions.getEndDate().plusDays(1).atStartOfDay()
        );
    }

    private BooleanExpression typeFilter(OrderSearchRequestDto.OrderSearchType type) {
        if (OrderSearchRequestDto.OrderSearchType.ALL.equals(type)) {
            return null;
        }
        return orderProduct.orderProductStatus.in(type.getTargets());
    }

    private BooleanExpression statusFilter(OrderProductStatus status) {
        if (status == null) {
            return null;
        }
        return orderProduct.orderProductStatus.eq(status);
    }

    private BooleanExpression adminTypeFilter(AdminOrderSearchRequestDto.OrderSearchType type) {
        if (AdminOrderSearchRequestDto.OrderSearchType.ALL.equals(type)) {
            return null;
        }
        if (AdminOrderSearchRequestDto.OrderSearchType.NORMAL.equals(type)) {
            return orderProduct.orderProductStatus.in(OrderProductStatus.PAYMENT_PENDING, OrderProductStatus.PAYMENT_COMPLETED, 
                    OrderProductStatus.PREPARING, OrderProductStatus.SHIPPING, OrderProductStatus.DELIVERED);
        }
        if (AdminOrderSearchRequestDto.OrderSearchType.CANCEL_RETURN_EXCHANGE.equals(type)) {
            return orderProduct.orderProductStatus.in(OrderProductStatus.ORDER_CANCELLED, OrderProductStatus.CANCEL_COMPLETED,
                    OrderProductStatus.RETURN_REQUEST, OrderProductStatus.RETURN_COMPLETED,
                    OrderProductStatus.EXCHANGE_REQUEST, OrderProductStatus.EXCHANGE_COMPLETED,
                    OrderProductStatus.PICKUP_IN_PROGRESS, OrderProductStatus.PICKED_UP);
        }
        return null;
    }

    private BooleanExpression adminStatusFilter(String status) {
        if (status == null || status.isEmpty() || "전체".equals(status)) {
            return null;
        }
        
        // OrderStatus로 변환 시도 (한글/영어 모두 지원)
        OrderStatus orderStatus = OrderStatus.fromString(status);
        if (orderStatus != null) {
            return order.orderStatus.eq(orderStatus);
        }
        
        return null;
    }

    /**
     * OrderStatus를 한글 레이블로 변환
     */
    private String getOrderStatusLabel(String orderStatusString) {
        if (orderStatusString == null) {
            return null;
        }
        
        try {
            OrderStatus orderStatus = OrderStatus.valueOf(orderStatusString);
            return orderStatus.getLabel();
        } catch (IllegalArgumentException e) {
            return orderStatusString;
        }
    }
}
