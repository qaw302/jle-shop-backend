package com.smplatform.product_service.domain.order.entity;

import lombok.Getter;

@Getter
public enum OrderStatus {
    PAYMENT_PENDING(1, "결제 대기"),
    PAYMENT_COMPLETED(2, "결제 완료"),
    SHIPPING(3, "배송 중"),
    DELIVERED(4, "배송 완료"),
    PAYMENT_FAILED(5, "결제 실패"),
    ORDER_CANCELLED(6, "주문 취소");

    private final int code;
    private final String label;

    OrderStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 문자열 값으로 enum을 찾습니다. 한글 레이블 또는 영어 열거형 이름을 지원합니다.
     */
    public static OrderStatus fromString(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        // 먼저 enum 이름으로 시도 (영어)
        try {
            return OrderStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            // enum 이름이 아니면 한글 레이블로 시도
            for (OrderStatus status : values()) {
                if (status.label.equals(value)) {
                    return status;
                }
            }
        }

        return null;
    }
}

