package com.smplatform.product_service.domain.coupon.dto;

import com.querydsl.core.annotations.QueryProjection;
import com.smplatform.product_service.domain.coupon.entity.MemberCoupon;
import lombok.Getter;

import java.time.LocalDateTime;

public class MemberCouponResponseDto {

    @Getter
    public static class CouponIssue {
        private Long memberCouponId;
        private Long couponId;

        public CouponIssue(long memberCouponId, long couponId) {
            this.memberCouponId = memberCouponId;
            this.couponId = couponId;
        }

        public static CouponIssue of(MemberCoupon memberCoupon) {
            return new CouponIssue(memberCoupon.getMemberCouponId(), memberCoupon.getCoupon().getCouponId());
        }
    }

    @Getter
    public static class MemberCouponInfo {
        private Long memberCouponId;
        private String couponIssueCode;
        private String couponName;
        private String couponType;
        private Integer discountAmount;
        private Integer minOrderPrice;
        private Integer maxDiscountPrice;
        private LocalDateTime issuedAt;
        private LocalDateTime couponStartDate;
        private LocalDateTime couponEndDate;
        private String comment;
        private String status;
        private Long couponId;

        @QueryProjection
        public MemberCouponInfo(Long memberCouponId,
                                String couponIssueCode,
                                String couponName,
                                String couponType,
                                Integer discountAmount,
                                Integer minOrderPrice,
                                Integer maxDiscountPrice,
                                LocalDateTime issuedAt,
                                LocalDateTime couponStartDate,
                                LocalDateTime couponEndDate,
                                String comment,
                                String status,
                                Long couponId) {
            this.memberCouponId = memberCouponId;
            this.couponIssueCode = couponIssueCode;
            this.couponName = couponName;
            this.couponType = couponType;
            this.discountAmount = discountAmount;
            this.minOrderPrice = minOrderPrice;
            this.maxDiscountPrice = maxDiscountPrice;
            this.issuedAt = issuedAt;
            this.couponStartDate = couponStartDate;
            this.couponEndDate = couponEndDate;
            this.comment = comment;
            this.status = status;
            this.couponId = couponId;
        }

    }
}
