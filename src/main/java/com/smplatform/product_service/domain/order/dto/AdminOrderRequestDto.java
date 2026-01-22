package com.smplatform.product_service.domain.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class AdminOrderRequestDto {

    @Getter
    @NoArgsConstructor
    public static class UpdateStatus {
        @NotNull
        private Long orderId;
        @NotNull
        private String orderStatus;
    }
}