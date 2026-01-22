package com.smplatform.product_service.domain.coupon.dto;

import com.smplatform.product_service.domain.coupon.entity.CouponType;
import com.smplatform.product_service.domain.coupon.entity.IssueType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDateTime;

public class CouponRequestDto {

    @Getter
    public static class CouponCreate {
        @NotBlank
        private String couponName;
        @NotNull
        private CouponType couponType;
        @NotNull
        private Integer amount;
        private Integer minOrderPrice;
        private Integer maxDiscountPrice;
        private LocalDateTime couponStartDate;
        private LocalDateTime couponEndDate;
        @NotNull
        private IssueType issueType;
        private String couponIssueCode;
        private String comment;
    }

    @Getter
    public static class CouponSearch {
        private String couponName;
        private LocalDateTime couponStartDate;
        private LocalDateTime couponEndDate;
    }

    @Getter
    public static class CouponDelete {
        @NotNull
        private Long couponId;
    }
}
