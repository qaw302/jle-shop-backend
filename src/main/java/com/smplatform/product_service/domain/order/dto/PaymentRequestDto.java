package com.smplatform.product_service.domain.order.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class PaymentRequestDto {
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PaymentConfirm {
        private String paymentKey;
        
        // ⚠️ 주의: 토스 페이먼츠에서는 이 필드명을 "orderId"로 보내지만,
        // 실제로는 주문번호(orderNumber)를 의미합니다.
        // 토스 API 스펙에 맞추기 위해 필드명은 orderId로 유지하되,
        // getter로 orderNumber도 제공합니다.
        private String orderId;
        
        private Long amount;
        
        /**
         * orderId는 실제로 orderNumber(주문번호)를 의미합니다.
         * 예: ORD20250120143025001
         */
        public String getOrderNumber() {
            return this.orderId;
        }
    }
}
