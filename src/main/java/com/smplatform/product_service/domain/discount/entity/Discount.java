package com.smplatform.product_service.domain.discount.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Arrays;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "discounts")
public class Discount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long discountId;
    @Column(name = "discount_name")
    private String discountName;
    @Column(name = "discount_type")
    @Enumerated(EnumType.STRING)
    private Type discountType;
    @Column(name = "discount_value")
    private int discountValue;
    @Column(name = "discount_start_date")
    private LocalDateTime discountStartDate;
    @Column(name = "discount_end_date")
    private LocalDateTime discountEndDate;

    @Getter
    @RequiredArgsConstructor
    public enum Type {
        RATE("할인율"),
        AMOUNT("할인금액");

        private final String description;

        public static Type fromDescription(String description) {
            return Arrays.stream(Type.values())
                    .filter(type -> type.description.equals(description))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Invalid discount type: " + description));
        }
    }

    @Getter
    public enum ApplyType {
        ALL(0),
        SPECIFIC_PRODUCT(1),
        SPECIFIC_CATEGORY(2);

        private final int typeNum;

        ApplyType(int typeNum) {
            this.typeNum = typeNum;
        }

        public static ApplyType fromTypeNum(int typeNum) {
            return Arrays.stream(ApplyType.values())
                    .filter(type -> typeNum == type.getTypeNum())
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("올바르지 않은 할인 적용 구분"));
        }
    }

    public boolean isValidDiscount() {
        LocalDateTime now = LocalDateTime.now();
        if (discountStartDate == null || discountEndDate == null) {
            return false;
        }
        if (now.isBefore(discountStartDate) || now.isAfter(discountEndDate)) {
            return false;
        }
        return true;
    }

    public int calculateDiscountedPrice(int originalPrice) {
        if (originalPrice <= 0) {
            throw new IllegalArgumentException("Original must not be smaller than 0");
        }

        int discountedPrice = 0;
        switch (discountType) {
            // 15 / 100 = 0.15 * 1000
            case RATE -> discountedPrice = (int) (originalPrice - originalPrice * ((double) this.discountValue / 100));
            case AMOUNT -> discountedPrice = originalPrice - this.discountValue;
        }
        return discountedPrice;
    }

}
