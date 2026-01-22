package com.smplatform.product_service.domain.order.service.impl;

import com.smplatform.product_service.domain.cart.repository.CartItemRepository;
import com.smplatform.product_service.domain.coupon.dto.MemberCouponResponseDto;
import com.smplatform.product_service.domain.coupon.entity.Coupon;
import com.smplatform.product_service.domain.coupon.entity.MemberCoupon;
import com.smplatform.product_service.domain.coupon.exception.CouponNotFoundException;
import com.smplatform.product_service.domain.coupon.repository.CouponRepository;
import com.smplatform.product_service.domain.coupon.repository.MemberCouponRepository;
import com.smplatform.product_service.domain.coupon.service.MemberCouponService;
import com.smplatform.product_service.domain.member.entity.Delivery;
import com.smplatform.product_service.domain.member.entity.Member;
import com.smplatform.product_service.domain.member.repository.DeliveryRepository;
import com.smplatform.product_service.domain.member.repository.MemberRepository;
import com.smplatform.product_service.domain.order.dto.AdminOrderRequestDto;
import com.smplatform.product_service.domain.order.dto.OrderRequestDto;
import com.smplatform.product_service.domain.order.dto.OrderResponseDto;
import com.smplatform.product_service.domain.order.entity.Order;
import com.smplatform.product_service.domain.order.entity.OrderBenefit;
import com.smplatform.product_service.domain.order.entity.OrderProduct;
import com.smplatform.product_service.domain.order.entity.OrderProductStatus;
import com.smplatform.product_service.domain.order.entity.OrderStatus;
import com.smplatform.product_service.domain.order.exception.OrderNotFoundException;
import com.smplatform.product_service.domain.order.repository.OrderBenefitRepository;
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
    private final OrderBenefitRepository orderBenefitRepository;
    private final MemberCouponRepository memberCouponRepository;

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
        MemberCoupon usedMemberCoupon = null;

        // 사용자 쿠폰 및 포인트 불러오기, 쿠폰 및 포인트 적용 가능한지 검증
        if (requestDto.getOrderDiscount() != null) {
            // 쿠폰 처리
            if (requestDto.getOrderDiscount().getCouponId() != null) {
                List<MemberCouponResponseDto.MemberCouponInfo> memberCoupons = memberCouponService.getCoupons(memberId);
                MemberCouponResponseDto.MemberCouponInfo matchedCoupon = memberCoupons.stream()
                        .filter(mc -> mc.getCouponId().equals(requestDto.getOrderDiscount().getCouponId()))
                        .findFirst()
                        .orElseThrow(() -> new CouponNotFoundException(
                                String.format("%s 회원님은 %s 쿠폰을 가지고 있지 않습니다.", memberId,
                                        requestDto.getOrderDiscount().getCouponId())));

                Coupon coupon = couponRepository.findById(requestDto.getOrderDiscount().getCouponId())
                        .orElseThrow(() -> new CouponNotFoundException(
                                String.format("coupon : %s not found", requestDto.getOrderDiscount().getCouponId())));

                if (!coupon.isAvailable()) {
                    throw new IllegalArgumentException("coupon 사용 가능 기간을 넘거나 도달 하지 못했습니다.");
                }
                couponDiscount = coupon.calculateDiscountedPrice(discountedTotalPrice.get());
                
                // OrderBenefit 저장을 위해 MemberCoupon 조회
                usedMemberCoupon = memberCouponRepository.findById(matchedCoupon.getMemberCouponId())
                        .orElseThrow(() -> new CouponNotFoundException(
                                String.format("memberCoupon : %s not found", matchedCoupon.getMemberCouponId())));
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
                new Order(null, null, member, null, finalPrice, OrderStatus.PAYMENT_PENDING, orderTitle));
        Delivery delivery = deliveryRepository.save(
                new Delivery(
                        0L,
                        null,
                        null,
                        requestDto.getOrderDetail().getShippingFee(),
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
                            savedDtoMap.get(orderItem.getProductOptionId()).getDiscountValue(),
                            (savedDtoMap.get(orderItem.getProductOptionId()).getPrice() + savedDtoMap.get(orderItem.getProductOptionId()).getAdditionalPrice()) * orderItem.getQuantity()));
        });

        // 상품 총할인금액 계산
        int productTotalDiscountAmount = originalTotalPrice.get() - discountedTotalPrice.get();

        // OrderBenefit 저장 (쿠폰 또는 포인트 사용 시)
        if (couponDiscount > 0 || pointDiscount > 0) {
            Long totalBenefit = (long) (couponDiscount + pointDiscount);
            OrderBenefit orderBenefit = new OrderBenefit(
                    null, // @MapsId를 사용하므로 orderId는 null로 설정
                    order,
                    pointDiscount,
                    couponDiscount,
                    usedMemberCoupon,
                    totalBenefit,
                    originalTotalPrice.get(),
                    productTotalDiscountAmount);
            orderBenefitRepository.save(orderBenefit);
            if (couponDiscount > 0 && usedMemberCoupon != null) {
                usedMemberCoupon.markUsed();
            }
        } else {
            // 혜택이 없어도 원가 정보는 저장
            OrderBenefit orderBenefit = new OrderBenefit(
                    null,
                    order,
                    0,
                    0,
                    null,
                    0L,
                    originalTotalPrice.get(),
                    productTotalDiscountAmount);
            orderBenefitRepository.save(orderBenefit);
        }

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

        return createOrderDetailResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDto.OrderDetail getAdminOrderDetail(Long orderId) {
        // 주문 조회
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("주문을 찾을 수 없습니다: " + orderId));

        return createOrderDetailResponse(order);
    }

    private OrderResponseDto.OrderDetail createOrderDetailResponse(Order order) {
        // 주문 상품 목록 조회
        List<OrderProduct> orderProducts = orderProductRepository.findAllByOrder(order);

        // 상품 정보 DTO 변환
        List<OrderResponseDto.OrderProduct> products = orderProducts.stream()
                .map(op -> {
                    // 옵션명을 파싱 (예: "화이트 / M" -> ["화이트", "M"])
                    List<OrderResponseDto.ProductOption> options = new java.util.ArrayList<>();
                    if (op.getProductOptionName() != null) {
                        String[] optionParts = op.getProductOptionName().split(" / ");
                        for (String optionPart : optionParts) {
                            options.add(new OrderResponseDto.ProductOption(optionPart.trim()));
                        }
                    }

                    OrderResponseDto.ProductInfo productInfo = new OrderResponseDto.ProductInfo(
                            op.getProductId(),
                            op.getProductName(),
                            options);

                    return new OrderResponseDto.OrderProduct(
                            productInfo,
                            op.getQuantity(),
                            op.getOrderPrice(),
                            op.getDiscountType() != null ? op.getDiscountType().toString() : null,
                            op.getDiscountValue());
                })
                .collect(Collectors.toList());

        // 배송 정보 조회
        Delivery delivery = orderProducts.stream()
                .findFirst()
                .map(OrderProduct::getDeliveryId)
                .orElse(null);

        OrderResponseDto.DeliveryInfo deliveryInfo = null;
        int shippingFee = 0;
        if (delivery != null) {
            String address = String.format("%s %s (%s)",
                    delivery.getAddress1(),
                    delivery.getAddress2() != null ? delivery.getAddress2() : "",
                    delivery.getPostalCode()).trim();
            deliveryInfo = new OrderResponseDto.DeliveryInfo(
                    address,
                    delivery.getRecipient(),
                    delivery.getPhoneNumber());
            shippingFee = delivery.getShippingFee() != null ? delivery.getShippingFee() : 0;
        }

        // OrderBenefit 조회
        com.smplatform.product_service.domain.order.entity.OrderBenefit orderBenefit = orderBenefitRepository.findById(order.getOrderId())
                .orElse(null);

        int productOriginalTotalPrice = 0;
        int productTotalDiscountAmount = 0;
        int couponDiscountAmount = 0;
        int pointUsedAmount = 0;

        if (orderBenefit != null) {
            productOriginalTotalPrice = orderBenefit.getProductOriginalTotalPrice() != null ? orderBenefit.getProductOriginalTotalPrice() : 0;
            productTotalDiscountAmount = orderBenefit.getProductTotalDiscountAmount() != null ? orderBenefit.getProductTotalDiscountAmount() : 0;
            couponDiscountAmount = orderBenefit.getCouponDiscount() != null ? orderBenefit.getCouponDiscount() : 0;
            pointUsedAmount = orderBenefit.getPointDiscount() != null ? orderBenefit.getPointDiscount() : 0;
        }

        int paymentPrice = order.getTotalPrice() != null ? order.getTotalPrice() : 0;

        return new OrderResponseDto.OrderDetail(
                order.getOrderId(),
                order.getOrderTitle(),
                order.getOrderDate(),
                productOriginalTotalPrice,
                productTotalDiscountAmount,
                couponDiscountAmount,
                pointUsedAmount,
                shippingFee,
                paymentPrice,
                products,
                toOrderStatusLabel(order.getOrderStatus()),
                deliveryInfo);
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
        if (order.getOrderStatus() == OrderStatus.DELIVERED) {
            throw new IllegalArgumentException("이미 완료된 주문은 취소할 수 없습니다.");
        }
        if (order.getOrderStatus() == OrderStatus.ORDER_CANCELLED) {
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

        return createOrderDetailResponse(order);
    }

    private String toOrderStatusLabel(OrderStatus status) {
        if (status == null) {
            return "상태 없음";
        }
        return switch (status) {
            case PAYMENT_PENDING -> "결제 대기";
            case PAYMENT_COMPLETED -> "결제 완료";
            case SHIPPING -> "배송 중";
            case DELIVERED -> "배송 완료";
            case PAYMENT_FAILED -> "결제 실패";
            case ORDER_CANCELLED -> "주문 취소";
        };
    }

    @Override
    @Transactional
    public void updateOrderStatus(AdminOrderRequestDto.UpdateStatus request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new OrderNotFoundException("주문을 찾을 수 없습니다: " + request.getOrderId()));

        OrderStatus newStatus;
        try {
            // 요청된 문자열을 OrderStatus Enum으로 변환합니다.
            newStatus = OrderStatus.valueOf(request.getOrderStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("유효하지 않은 주문 상태입니다: " + request.getOrderStatus());
        }

        // Order 엔티티의 상태를 업데이트합니다.
        order.updateStatus(newStatus);

        // Order의 상태 변경에 따라 OrderProduct들의 상태도 동기화.
        updateOrderProductsStatus(order, newStatus);
    }

    /**
     * 주문의 상태 변경에 따라 하위 주문 상품들의 상태를 업데이트합니다.
     * @param order 상태가 변경된 주문 엔티티
     * @param newOrderStatus 새로운 주문 상태
     */
    private void updateOrderProductsStatus(Order order, OrderStatus newOrderStatus) {
        List<OrderProduct> orderProducts = orderProductRepository.findAllByOrder(order);
        OrderProductStatus newProductStatus = mapOrderStatusToProductStatus(newOrderStatus);

        if (newProductStatus != null) {
            for (OrderProduct product : orderProducts) {
                // 이미 취소/반품/교환 처리된 상품의 상태는 변경하지 않습니다.
                if (product.getOrderProductStatus().getType() != OrderProductStatus.StatusType.CANCEL_RETURN_EXCHANGE) {
                    product.updateStatus(newProductStatus);
                }
            }
        }
    }

    private OrderProductStatus mapOrderStatusToProductStatus(OrderStatus orderStatus) {
        return switch (orderStatus) {
            case PAYMENT_COMPLETED -> OrderProductStatus.PAYMENT_COMPLETED;
            // case PREPARING_FOR_DELIVERY -> OrderProductStatus.PREPARING_FOR_DELIVERY;
            case SHIPPING -> OrderProductStatus.SHIPPING;
            case DELIVERED -> OrderProductStatus.DELIVERED;
            case PAYMENT_FAILED -> OrderProductStatus.PAYMENT_FAILED;
            case ORDER_CANCELLED -> OrderProductStatus.CANCEL_COMPLETED;
            default -> null;
        };
    }
}
