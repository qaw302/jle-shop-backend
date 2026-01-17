package com.smplatform.product_service.domain.order.repository.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.smplatform.product_service.domain.order.dto.OrderSearchRequestDto;
import com.smplatform.product_service.domain.order.dto.OrderSearchResponseDto;
import com.smplatform.product_service.domain.order.dto.QOrderSearchResponseDto_MemberOrder;
import com.smplatform.product_service.domain.order.entity.OrderProduct;
import com.smplatform.product_service.domain.order.entity.OrderProductStatus;
import com.smplatform.product_service.domain.order.entity.QOrder;
import com.smplatform.product_service.domain.order.entity.QOrderProduct;
import com.smplatform.product_service.domain.order.repository.CustomOrderSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

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
}
