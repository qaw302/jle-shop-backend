package com.smplatform.product_service.domain.order.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smplatform.product_service.domain.discount.entity.Discount;
import com.smplatform.product_service.domain.order.entity.Order;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

public class OrderResponseDto {
    @Getter
    @Schema(name = "ProductOptionFlatResponse", description = "주문 상품 옵션 요약 정보")
    public static class ProductOptionFlatDto {
        @Schema(description = "상품 옵션 ID", example = "300")
        private final Long productOptionId;
        @Schema(description = "상품 옵션명", example = "RED / M")
        private final String productOptionName;
        @Schema(description = "재고 수량", example = "50")
        private final int stockQuantity;
        @Schema(description = "추가 금액", example = "1000")
        private final int additionalPrice;

        @Schema(description = "상품 ID", example = "200")
        private final Long productId;
        @Schema(description = "상품명", example = "Basic Tee")
        private final String name;
        @Schema(description = "삭제 여부", example = "false")
        private final boolean isDeleted;
        @Schema(description = "판매 여부", example = "true")
        private final boolean isSelling;
        @Schema(description = "상품 가격", example = "25000")
        private final int price;

        @Schema(description = "할인 ID", example = "10")
        private final Long discountId;
        @Schema(description = "할인 타입", example = "RATE")
        private final Discount.Type discountType;
        @Schema(description = "할인 값", example = "10")
        private final int discountValue;
        @Schema(description = "할인 시작 시각", example = "2024-01-01T00:00:00")
        private final LocalDateTime discountStartDate;
        @Schema(description = "할인 종료 시각", example = "2024-01-31T23:59:59")
        private final LocalDateTime discountEndDate;
        @Setter
        @Schema(description = "옵션 단위 결제 금액", example = "23000")
        private Integer unitTotalPrice;

        public ProductOptionFlatDto(Long productOptionId, String productOptionName, int stockQuantity, int additionalPrice,
                                    Long productId, String name, boolean isDeleted, boolean isSelling, int price,
                                    Long discountId, Discount.Type discountType, int discountValue,
                                    LocalDateTime discountStartDate, LocalDateTime discountEndDate) {
            this.productOptionId = productOptionId;
            this.productOptionName = productOptionName;
            this.stockQuantity = stockQuantity;
            this.additionalPrice = additionalPrice;
            this.productId = productId;
            this.name = name;
            this.isDeleted = isDeleted;
            this.isSelling = isSelling;
            this.price = price;
            this.discountId = discountId;
            this.discountType = discountType;
            this.discountValue = discountValue;
            this.discountStartDate = discountStartDate;
            this.discountEndDate = discountEndDate;
        }
    }

    @Getter
    @AllArgsConstructor
    @Schema(name = "OrderDetailResponse", description = "주문 상세 응답")
    public static class OrderDetail {
        @Schema(description = "주문 ID", example = "1")
        private Long orderId;
        @Schema(description = "주문 타이틀", example = "예쁜 목도리 외 1건")
        private String orderTitle;
        @Schema(description = "주문 일시", example = "2024-01-02T10:30:00")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime orderDate;
        @Schema(description = "주문 금액(할인 미포함)", example = "20000")
        private Integer orderPrice;
        @Schema(description = "배송비", example = "3000")
        private Integer shippingFee;
        @Schema(description = "주문 혜택 정보 (쿠폰/포인트)")
        private OrderBenefit benefits;
        @Schema(description = "결제 금액", example = "16000")
        private Integer paymentPrice;
        @Schema(description = "주문 상품 목록")
        private List<OrderProduct> products;
        @Schema(description = "주문 상태", example = "결제완료")
        private String orderStatus;
        @Schema(description = "배송 정보")
        private DeliveryInfo deliveryInfo;
    }

    @Getter
    @AllArgsConstructor
    @Schema(name = "OrderProductResponse", description = "주문 상품 응답")
    public static class OrderProduct {
        @Schema(description = "상품 정보")
        private ProductInfo productInfo;
        @Schema(description = "수량", example = "2")
        private Integer quantity;
        @Schema(description = "상품 가격", example = "10000")
        private Integer price;
        @Schema(description = "할인 타입", example = "FIXED")
        private String discountType;
        @Schema(description = "할인 값", example = "1000")
        private Integer discountValue;
    }

    @Getter
    @AllArgsConstructor
    @Schema(name = "ProductInfoResponse", description = "상품 정보 응답")
    public static class ProductInfo {
        @Schema(description = "상품 ID", example = "1")
        private Long productId;
        @Schema(description = "상품명", example = "기본티")
        private String productName;
        @Schema(description = "상품 옵션 목록")
        private List<ProductOption> options;
    }

    @Getter
    @AllArgsConstructor
    @Schema(name = "ProductOptionResponse", description = "상품 옵션 응답")
    public static class ProductOption {
        @Schema(description = "옵션명", example = "화이트")
        private String optionName;
    }

    @Getter
    @AllArgsConstructor
    @Schema(name = "OrderBenefitResponse", description = "주문 혜택 정보 응답")
    public static class OrderBenefit {
        @Schema(description = "포인트 할인 금액", example = "1000")
        private Integer pointDiscount;
        @Schema(description = "쿠폰 할인 금액", example = "2000")
        private Integer couponDiscount;
    }

    @Getter
    @AllArgsConstructor
    @Schema(name = "DeliveryInfoResponse", description = "배송 정보 응답")
    public static class DeliveryInfo {
        @Schema(description = "배송 주소 (우편번호 포함)", example = "경기도 수원시 팔달구 덕영대로 (12345)")
        private String address;
        @Schema(description = "수령인", example = "김철수")
        private String recipient;
        @Schema(description = "전화번호", example = "010-1234-5678")
        private String phone;
    }

    @Getter
    @AllArgsConstructor
    @Schema(name = "OrderSaveSuccessResponse", description = "주문 생성 성공 응답")
    public static class OrderSaveSuccess {
        @Schema(description = "주문 번호", example = "ORD-20240101-0001")
        private String orderNumber;

        public static OrderSaveSuccess of(Order order) {
            return new OrderSaveSuccess(order.getOrderNumber());
        }
    }
}
