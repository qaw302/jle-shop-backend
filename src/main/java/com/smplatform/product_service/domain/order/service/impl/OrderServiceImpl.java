package com.smplatform.product_service.domain.order.service.impl;

import com.smplatform.product_service.domain.cart.repository.CartItemRepository;
import com.smplatform.product_service.domain.coupon.dto.MemberCouponResponseDto;
import com.smplatform.product_service.domain.coupon.entity.Coupon;
import com.smplatform.product_service.domain.coupon.exception.CouponNotFoundException;
import com.smplatform.product_service.domain.coupon.repository.CouponRepository;
import com.smplatform.product_service.domain.coupon.service.MemberCouponService;
import com.smplatform.product_service.domain.member.entity.Delivery;
import com.smplatform.product_service.domain.member.entity.Member;
import com.smplatform.product_service.domain.member.repository.DeliveryRepository;
import com.smplatform.product_service.domain.member.repository.MemberRepository;
import com.smplatform.product_service.domain.order.dto.OrderRequestDto;
import com.smplatform.product_service.domain.order.dto.OrderResponseDto;
import com.smplatform.product_service.domain.order.entity.Order;
import com.smplatform.product_service.domain.order.entity.OrderProduct;
import com.smplatform.product_service.domain.order.entity.OrderProductStatus;
import com.smplatform.product_service.domain.order.entity.OrderStatus;
import com.smplatform.product_service.domain.order.exception.OrderNotFoundException;
import com.smplatform.product_service.domain.order.repository.OrderProductRepository;
import com.smplatform.product_service.domain.order.repository.OrderRepository;
import com.smplatform.product_service.domain.order.service.OrderService;
import com.smplatform.product_service.domain.product.entity.ProductOption;
import com.smplatform.product_service.domain.product.exception.ProductNotFoundException;
import com.smplatform.product_service.domain.product.exception.ProductOptionNotFoundException;
import com.smplatform.product_service.domain.product.repository.ProductOptionRepository;
import com.smplatform.product_service.domain.product.repository.ProductRepository;
import com.smplatform.product_service.exception.BadRequestException;
import com.smplatform.product_service.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final ProductOptionRepository productOptionRepository;
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;
    private final DeliveryRepository deliveryRepository;
    private final MemberRepository memberRepository;
    private final OrderProductRepository orderProductRepository;
    private final CouponRepository couponRepository;
    private final MemberCouponService memberCouponService;

    @Override
    @Transactional
    public OrderResponseDto.OrderSaveSuccess saveOrder(String memberId, OrderRequestDto.OrderSave requestDto) {
        Map<Long, OrderRequestDto.OrderItem> requestDtoMap = new HashMap<>();
        Map<Long, OrderResponseDto.ProductOptionFlatDto> savedDtoMap = new HashMap<>();

        Member member = memberRepository.findById(memberId).orElse(null);

        List<OrderResponseDto.ProductOptionFlatDto> productOptionFlatDtos = orderRepository.findAllByProductOptionIds(
                requestDto.getItems().stream().map(orderItem -> {
                    requestDtoMap.put(orderItem.getProductOptionId(), orderItem);
                    return orderItem.getProductOptionId();
                }).toList());

        // 할인 정보 불러오기, 실제 제품에 할인 가격 적용
        AtomicInteger originalTotalPrice = new AtomicInteger();
        AtomicInteger discountedTotalPrice = new AtomicInteger();
        productOptionFlatDtos.forEach(unit -> {
            if (requestDtoMap.get(unit.getProductOptionId()).getQuantity() > unit.getStockQuantity()) {
                throw new IllegalArgumentException("재고 수량보다 주문 수량이 많습니다. productOptionId="
                        + unit.getProductOptionId()
                        + ", 요청=" + requestDtoMap.get(unit.getProductOptionId()).getQuantity()
                        + ", 재고=" + unit.getStockQuantity());
            }

            originalTotalPrice.addAndGet((unit.getPrice() + unit.getAdditionalPrice())
                    * requestDtoMap.get(unit.getProductOptionId()).getQuantity());

            if (!Objects.isNull(unit.getDiscountId())) {
                int unitPrice = productRepository.findByProductId(unit.getProductId())
                        .orElseThrow(() -> new ProductNotFoundException("product not found"))
                        .getDiscountedPrice() + unit.getAdditionalPrice();
                log.debug("original unit price : " + unitPrice);
                unit.setUnitTotalPrice(unitPrice);
                discountedTotalPrice.addAndGet(unitPrice * requestDtoMap.get(unit.getProductOptionId()).getQuantity());
            } else {
                unit.setUnitTotalPrice(unit.getPrice() + unit.getAdditionalPrice());
                discountedTotalPrice.addAndGet(
                        (unit.getPrice() + unit.getAdditionalPrice())
                                * requestDtoMap.get(unit.getProductOptionId()).getQuantity());
            }
            savedDtoMap.put(unit.getProductOptionId(), unit);
        });

        int couponDiscount = 0;
        int pointDiscount = 0;

        // 사용자 쿠폰 및 포인트 불러오기, 쿠폰 및 포인트 적용 가능한지 검증
        if (requestDto.getOrderDiscount() != null) {
            // 쿠폰 처리
            if (requestDto.getOrderDiscount().getCouponId() != null) {
                if (!memberCouponService.getCoupons(memberId).stream()
                        .map(MemberCouponResponseDto.MemberCouponInfo::getCouponId).toList()
                        .contains(requestDto.getOrderDiscount().getCouponId())) {
                    throw new CouponNotFoundException(
                            String.format("%s 회원님은 %s 쿠폰을 가지고 있지 않습니다.", memberId,
                                    requestDto.getOrderDiscount().getCouponId()));
                }
                Coupon coupon = couponRepository.findById(requestDto.getOrderDiscount().getCouponId())
                        .orElseThrow(() -> new CouponNotFoundException(
                                String.format("coupon : %s not found", requestDto.getOrderDiscount().getCouponId())));

                if (!coupon.isAvailable()) {
                    throw new IllegalArgumentException("coupon 사용 가능 기간을 넘거나 도달 하지 못했습니다.");
                }
                couponDiscount = coupon.calculateDiscountedPrice(discountedTotalPrice.get());
            }

            // 포인트 처리
            // if (requestDto.getOrderDiscount().getPoints() != null &&
            // requestDto.getOrderDiscount().getPoints() > 0) {
            // if (member.getPoints() == null || member.getPoints() <
            // requestDto.getOrderDiscount().getPoints()) {
            // throw new IllegalArgumentException(
            // String.format("포인트가 부족합니다. 보유 포인트: %d, 사용 요청: %d",
            // member.getPoints() != null ? member.getPoints() : 0,
            // requestDto.getOrderDiscount().getPoints())
            // );
            // }
            // pointDiscount = requestDto.getOrderDiscount().getPoints();
            // // 포인트 차감
            // member.usePoints(pointDiscount);
            // }
        }

        // 실제 가격 계산 및 검증
        int finalPrice = discountedTotalPrice.get() - couponDiscount - pointDiscount
                + requestDto.getOrderDetail().getShippingFee();

        log.debug("최종 가격 : " + finalPrice);
        if (requestDto.getOrderDetail().getFinalAmount() != finalPrice) {
            log.error("주문 가격 불일치 - memberId: {}, 요청가격: {}, 계산가격: {}, 상세정보 [할인된상품금액: {}, 쿠폰할인: {}, 포인트할인: {}, 배송비: {}]",
                    memberId,
                    requestDto.getOrderDetail().getFinalAmount(),
                    finalPrice,
                    discountedTotalPrice.get(),
                    couponDiscount,
                    pointDiscount,
                    requestDto.getOrderDetail().getShippingFee());
            throw new BadRequestException(String.format("주문 가격이 일치하지 않습니다 (요청: %d, 계산: %d)",
                    requestDto.getOrderDetail().getFinalAmount(), finalPrice));
        }

        String orderTitle = productOptionRepository.findById(requestDto.getItems().get(0).getProductOptionId())
                .orElseThrow(() -> new ProductOptionNotFoundException(
                        String.format("제품 %s이 존재하지 않습니다.", requestDto.getItems().get(0).getProductOptionId())))
                .getProduct().getName();

        if (requestDto.getItems().size() > 1) {
            orderTitle = String.format("%s 외 %s 종", orderTitle, requestDto.getItems().size());
        }

        // 할인 및 배송 엔티티 저장
        Order order = orderRepository.save(
                new Order(null, null, member, null, finalPrice, OrderStatus.PROGRESSING, orderTitle));
        Delivery delivery = deliveryRepository.save(
                new Delivery(
                        0L,
                        null,
                        null,
                        null,
                        requestDto.getAddressInfo().getPostalCode(),
                        requestDto.getAddressInfo().getAddress1(),
                        requestDto.getAddressInfo().getAddress2(),
                        requestDto.getAddressInfo().getReceiver(),
                        requestDto.getAddressInfo().getPhoneNumber()));

        // 장바구니 아이템이라면 장바구니 아이템 삭제
        requestDto.getItems().forEach(orderItem -> {
            if (!Objects.isNull(orderItem.getCartItemId())) {
                cartItemRepository.deleteById(orderItem.getCartItemId());
            }
            orderProductRepository.save(
                    new OrderProduct(
                            null,
                            order,
                            delivery,
                            orderItem.getQuantity(),
                            savedDtoMap.get(orderItem.getProductOptionId()).getUnitTotalPrice(),
                            OrderProductStatus.PAYMENT_PENDING,
                            savedDtoMap.get(orderItem.getProductOptionId()).getProductId(),
                            savedDtoMap.get(orderItem.getProductOptionId()).getProductOptionId(),
                            savedDtoMap.get(orderItem.getProductOptionId()).getProductOptionName(),
                            savedDtoMap.get(orderItem.getProductOptionId()).getName(),
                            savedDtoMap.get(orderItem.getProductOptionId()).getDiscountId(),
                            savedDtoMap.get(orderItem.getProductOptionId()).getDiscountType(),
                            savedDtoMap.get(orderItem.getProductOptionId()).getDiscountValue()));
        });

        return OrderResponseDto.OrderSaveSuccess.of(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDto.OrderDetail getOrderDetail(String memberId, Long orderId) {
        // 주문 조회
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("주문을 찾을 수 없습니다: " + orderId));

        // 권한 확인 (본인의 주문만 조회 가능)
        if (!order.getMember().getMemberId().equals(memberId)) {
            throw new UnauthorizedException("본인의 주문만 조회할 수 있습니다.");
        }

        // 주문 상품 목록 조회
        List<OrderProduct> orderProducts = orderProductRepository.findAllByOrder(order);

        // DTO 변환
        List<OrderResponseDto.OrderProductInfo> productInfos = orderProducts.stream()
                .map(op -> new OrderResponseDto.OrderProductInfo(
                        op.getOrderProductId(),
                        op.getProductName(),
                        op.getProductOptionName(),
                        op.getQuantity(),
                        op.getOrderPrice(),
                        op.getOrderProductStatus() != null ? op.getOrderProductStatus().getLabel() : "상태 없음"))
                .collect(Collectors.toList());

        return new OrderResponseDto.OrderDetail(
                order.getOrderId(),
                order.getOrderTitle(),
                order.getOrderDate(),
                order.getTotalPrice(),
                order.getOrderStatus(),
                productInfos);
    }

    @Override
    @Transactional
    public void cancelOrder(String memberId, Long orderId) {
        // 주문 조회
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderId));

        // 권한 확인 (본인의 주문만 취소 가능)
        if (!order.getMember().getMemberId().equals(memberId)) {
            throw new IllegalArgumentException("본인의 주문만 취소할 수 있습니다.");
        }

        // 취소 가능한 상태인지 확인
        if (order.getOrderStatus() == OrderStatus.COMPLETE) {
            throw new IllegalArgumentException("이미 완료된 주문은 취소할 수 없습니다.");
        }
        if (order.getOrderStatus() == OrderStatus.CANCEL) {
            throw new IllegalArgumentException("이미 취소된 주문입니다.");
        }

        // 주문 상태를 취소로 변경
        order.cancelOrder();

        // 주문 상품 목록 조회 및 재고 복구
        List<OrderProduct> orderProducts = orderProductRepository.findAllByOrder(order);
        for (OrderProduct orderProduct : orderProducts) {
            // 결제 완료 상태인 경우에만 재고 복구
            if (orderProduct.getOrderProductStatus() != null &&
                    orderProduct.getOrderProductStatus()
                            .getType() != OrderProductStatus.StatusType.CANCEL_RETURN_EXCHANGE) {

                ProductOption productOption = productOptionRepository.findById(orderProduct.getProductOptionId())
                        .orElseThrow(() -> new ProductOptionNotFoundException(
                                "상품 옵션을 찾을 수 없습니다: " + orderProduct.getProductOptionId()));

                // 재고 복구
                productOption.increaseStock(orderProduct.getQuantity());
            }

            // 주문 상품 상태를 취소 완료로 변경
            orderProduct.updateStatus(OrderProductStatus.CANCEL_COMPLETED);
        }

        // 포인트 복구 (포인트 사용했다면)
        // TODO: OrderBenefit에서 포인트 정보를 가져와서 복구하는 로직 추가 필요
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDto.OrderDetail getOrderDetailByOrderNumber(String memberId, String orderNumber) {
        // 주문 조회
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderNumber));

        // 권한 확인 (본인의 주문만 조회 가능)
        if (!order.getMember().getMemberId().equals(memberId)) {
            throw new IllegalArgumentException("본인의 주문만 조회할 수 있습니다.");
        }

        // 주문 상품 목록 조회
        List<OrderProduct> orderProducts = orderProductRepository.findAllByOrder(order);

        // DTO 변환
        List<OrderResponseDto.OrderProductInfo> productInfos = orderProducts.stream()
                .map(op -> new OrderResponseDto.OrderProductInfo(
                        op.getOrderProductId(),
                        op.getProductName(),
                        op.getProductOptionName(),
                        op.getQuantity(),
                        op.getOrderPrice(),
                        op.getOrderProductStatus() != null ? op.getOrderProductStatus().getLabel() : "상태 없음"))
                .collect(Collectors.toList());

        return new OrderResponseDto.OrderDetail(
                order.getOrderId(),
                order.getOrderTitle(),
                order.getOrderDate(),
                order.getTotalPrice(),
                order.getOrderStatus(),
                productInfos);
    }
}
