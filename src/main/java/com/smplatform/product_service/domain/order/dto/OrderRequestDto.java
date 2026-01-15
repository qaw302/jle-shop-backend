package com.smplatform.product_service.domain.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.List;

public class OrderRequestDto {
    @Getter
    @Schema(name = "OrderSaveRequest", description = "주문 생성 요청")
    public static class OrderSave {
        @Schema(description = "배송지 정보")
        private OrderRequestDto.OrderAddress addressInfo;
        @Schema(description = "주문 상품 목록")
        private List<OrderRequestDto.OrderItem> items;
        @Schema(description = "결제 수단", example = "CARD")
        private String paymentMethod;
        @Schema(description = "할인 정보")
        private OrderRequestDto.OrderDiscount orderDiscount;
        @Schema(description = "주문 금액 정보")
        private OrderRequestDto.OrderDetail orderDetail;
    }

    @Getter
    @Schema(name = "OrderAddressRequest", description = "주문 배송지 정보")
    public static class OrderAddress {
        @Schema(description = "기본 주소", example = "서울시 강남구 테헤란로 123")
        private String address1;
        @Schema(description = "상세 주소", example = "101호")
        private String address2;
        @Schema(description = "우편번호", example = "06236")
        private String postalCode;
        @Schema(description = "수령인", example = "홍길동")
        private String receiver;
        @Schema(description = "연락처", example = "01012345678")
        private String phoneNumber;
        @Schema(description = "이메일", example = "user@example.com")
        private String email;
    }

    @Getter
    @Schema(name = "OrderItemRequest", description = "주문 상품 정보")
    public static class OrderItem {
        @Schema(description = "장바구니 아이템 ID", example = "100")
        private Long cartItemId;
        @Schema(description = "상품 ID", example = "200")
        private Long productId;
        @Schema(description = "상품 옵션 ID", example = "300")
        private Long productOptionId;
        @Schema(description = "수량", example = "2")
        private int quantity;
    }

    @Getter
    @Schema(name = "OrderDiscountRequest", description = "주문 할인 정보")
    public static class OrderDiscount {
        @Schema(description = "쿠폰 ID", example = "10")
        private Long couponId;
        @Schema(description = "포인트", example = "1000")
        private Integer points;
    }

    @Getter
    @Schema(name = "OrderDetailRequest", description = "주문 금액 정보")
    public static class OrderDetail {
        @Schema(description = "상품 원가 합계", example = "50000")
        private int originalTotal;
        @Schema(description = "할인 적용 후 합계", example = "45000")
        private int discountedTotal;
        @Schema(description = "상품 할인 금액", example = "3000")
        private int productDiscount;
        @Schema(description = "추가 할인 금액", example = "2000")
        private int additionalDiscount;
        @Schema(description = "배송비", example = "2500")
        private int shippingFee;
        @Schema(description = "최종 결제 금액", example = "47500")
        private int finalAmount;
    }

}
