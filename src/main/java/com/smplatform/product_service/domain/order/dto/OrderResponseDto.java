package com.smplatform.product_service.domain.order.dto;

import com.smplatform.product_service.domain.discount.entity.Discount;
import com.smplatform.product_service.domain.order.entity.Order;
import com.smplatform.product_service.domain.order.entity.OrderStatus;
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
        @Schema(description = "주문 ID", example = "500")
        private Long orderId;
        @Schema(description = "주문 타이틀", example = "Basic Tee 외 1건")
        private String orderTitle;
        @Schema(description = "주문 일시", example = "2024-01-02T10:30:00")
        private LocalDateTime orderDate;
        @Schema(description = "총 결제 금액", example = "47500")
        private Integer totalPrice;
        @Schema(description = "주문 상태", example = "COMPLETE")
        private OrderStatus orderStatus;
        @Schema(description = "주문 상품 목록")
        private List<OrderProductInfo> products;
    }

    @Getter
    @AllArgsConstructor
    @Schema(name = "OrderProductInfoResponse", description = "주문 상품 응답")
    public static class OrderProductInfo {
        @Schema(description = "주문 상품 ID", example = "700")
        private Long orderProductId;
        @Schema(description = "상품명", example = "Basic Tee")
        private String productName;
        @Schema(description = "상품 옵션명", example = "RED / M")
        private String productOptionName;
        @Schema(description = "수량", example = "2")
        private Integer quantity;
        @Schema(description = "주문 금액", example = "23000")
        private Integer orderPrice;
        @Schema(description = "주문 상품 상태", example = "PAID")
        private String orderProductStatus;
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
