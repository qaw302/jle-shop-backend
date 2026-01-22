package com.smplatform.product_service.domain.order.dto;

import com.querydsl.core.annotations.QueryProjection;
import com.smplatform.product_service.domain.discount.entity.Discount;
import com.smplatform.product_service.domain.order.entity.OrderProduct;
import com.smplatform.product_service.domain.order.entity.OrderProductStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

public class OrderSearchResponseDto {

    @Getter
    @AllArgsConstructor
    @Schema(name = "MemberOrdersResponse", description = "회원 주문내역 조회 응답")
    public static class MemberOrdersGet {
        @Schema(description = "주문 목록")
        private List<MemberOrder> content;
        @Schema(description = "현재 페이지", example = "0")
        private int page;
        @Schema(description = "페이지 크기", example = "10")
        private int size;
        @Schema(description = "전체 요소 수", example = "25")
        private long totalElements;
        @Schema(description = "전체 페이지 수", example = "3")
        private int totalPages;
    }

    @Getter
    @AllArgsConstructor
    @Schema(name = "AdminOrdersResponse", description = "관리자 주문내역 조회 응답")
    public static class AdminOrdersGet {
        @Schema(description = "주문 목록")
        private List<AdminOrder> content;
        @Schema(description = "현재 페이지", example = "0")
        private int page;
        @Schema(description = "페이지 크기", example = "10")
        private int size;
        @Schema(description = "전체 요소 수", example = "25")
        private long totalElements;
        @Schema(description = "전체 페이지 수", example = "3")
        private int totalPages;
    }

    @Getter
    @Schema(name = "MemberOrderResponse", description = "회원 주문 요약")
    public static class MemberOrder {
        @Schema(description = "주문 ID", example = "500")
        private long orderId;
        @Schema(description = "주문 일시", example = "2024-01-02T10:30:00")
        private LocalDateTime orderDate;
        @Schema(description = "대표 상품명", example = "Basic Tee")
        private String mainProductName;
        @Schema(description = "상품 개수", example = "2")
        private int productCount;
        @Setter
        @Schema(description = "결제금액", example = "50000")
        private Integer paymentAmount;
        @Setter
        @Schema(description = "주문 상품 목록")
        private List<OrderProductDto> products;
        
        

        @QueryProjection
        public MemberOrder(long orderId, LocalDateTime orderDate, String productName, int productCount, Integer paymentAmount) {
            this.orderId = orderId;
            this.orderDate = orderDate;
            this.mainProductName = productName;
            this.productCount = productCount;
            this.paymentAmount = paymentAmount;
        }
    }

    @Getter
    @AllArgsConstructor
    @Schema(name = "OrderProductResponse", description = "주문 상품 정보")
    public static class OrderProductDto {
        @Schema(description = "상품 ID", example = "200")
        private Long productId;
        @Schema(description = "상품명", example = "Basic Tee")
        private String productName;
        @Schema(description = "옵션명", example = "RED / M")
        private String optionName;
        @Schema(description = "수량", example = "2")
        private int quantity;
        @Schema(description = "주문 금액", example = "23000")
        private long price;
        @Schema(description = "할인 ID", example = "10")
        private Long discountId;
        @Schema(description = "할인 타입", example = "RATE")
        private Discount.Type discountType;
        @Schema(description = "주문 상품 상태", example = "PAID")
        private OrderProductStatus orderProductStatus;

        public static OrderProductDto of(OrderProduct orderProduct) {
            return new OrderProductDto(
                    orderProduct.getProductId(),
                    orderProduct.getProductName(),
                    orderProduct.getProductOptionName(),
                    orderProduct.getQuantity(),
                    orderProduct.getOrderPrice(),
                    orderProduct.getDiscountId(),
                    orderProduct.getDiscountType(),
                    orderProduct.getOrderProductStatus());
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "AdminOrderResponse", description = "관리자 주문 조회")
    public static class AdminOrder {
        @Schema(description = "주문 ID", example = "1")
        private Long orderId;

        @Schema(description = "주문 일시", example = "2025-01-02 10:30:00")
        private String orderDate;

        @Schema(description = "주문 회원명", example = "제이르")
        private String orderMemberName;

        @Schema(description = "상품명", example = "Basic Tee")
        private String productName;

        @Schema(description = "총 가격", example = "10000")
        private Integer totalPrice;

        @Schema(description = "결제금액", example = "9000")
        private Integer paymentPrice;

        @Schema(description = "결제방법", example = "toss")
        private String paymentMethod;

        @Schema(description = "주문 상태", example = "결제 완료")
        private String orderStatus;

        @QueryProjection
        public AdminOrder(Long orderId, LocalDateTime orderDate, String orderMemberName, String productName,
                         Integer totalPrice, Integer paymentPrice, String paymentMethod, String orderStatus) {
            this.orderId = orderId;
            this.orderDate = orderDate != null ? orderDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null;
            this.orderMemberName = orderMemberName;
            this.productName = productName;
            this.totalPrice = totalPrice;
            this.paymentPrice = paymentPrice;
            this.paymentMethod = paymentMethod;
            this.orderStatus = orderStatus;
        }
    }
}
