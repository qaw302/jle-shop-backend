package com.smplatform.product_service.domain.discount.dto;

import com.smplatform.product_service.domain.discount.entity.Discount;
import lombok.*;

import java.time.LocalDateTime;

public class DiscountResponseDto {

    @Getter
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiscountInfo {

        private long discountId;

        private String discountName;

        private String discountType;

        private int discountValue;

        private LocalDateTime discountStartDate;

        private LocalDateTime discountEndDate;

        public static DiscountResponseDto.DiscountInfo of(Discount entity) {
            return new DiscountResponseDto.DiscountInfo(
                    entity.getDiscountId(),
                    entity.getDiscountName(),
                    entity.getDiscountType().getDescription(),
                    entity.getDiscountValue(),
                    entity.getDiscountStartDate(),
                    entity.getDiscountEndDate()
            );
        }
    }


}
