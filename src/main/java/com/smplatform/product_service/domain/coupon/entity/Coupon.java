package com.smplatform.product_service.domain.coupon.entity;

import com.smplatform.product_service.domain.coupon.dto.CouponRequestDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "coupons", indexes = @Index(name="uk_coupon_code", columnList = "coupon_code", unique = true))
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_id")
    private long couponId;

    @Column(name = "coupon_code", unique = true, nullable = false)
    private String couponCode;

    @Column(name = "issue_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private IssueType issueType;

    @Column(name = "coupon_name", nullable = false)
    private String couponName;

    @Column(name = "coupon_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private CouponType couponType;

    @Column(name = "discount_amount", nullable = false)
    private int discountAmount;

    @Column(name = "coupon_start_at")
    private LocalDateTime couponStartAt;

    @Column(name = "coupon_end_at")
    private LocalDateTime couponEndAt;

    @Column(name = "min_order_price")
    private int minOrderPrice;

    @Column(name = "max_discount_price")
    private int maxDiscountPrice;

    @Column(name = "commnet")
    private String comment;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    private Coupon(CouponRequestDto.CouponCreate couponCreateDto) {
        this.couponName = couponCreateDto.getCouponName();
        this.couponType = couponCreateDto.getCouponType();
        this.discountAmount = couponCreateDto.getAmount();
        this.minOrderPrice = couponCreateDto.getMinOrderPrice();
        this.maxDiscountPrice = couponCreateDto.getMaxDiscountPrice();
        this.couponStartAt = couponCreateDto.getCouponStartDate();
        this.couponEndAt = couponCreateDto.getCouponEndDate();
        this.issueType = couponCreateDto.getIssueType();
        if (IssueType.CODE.equals(issueType)) {
            this.couponCode = couponCreateDto.getCouponIssueCode();
        } else {
            this.couponCode = UUID.randomUUID().toString();
        }
        this.comment = couponCreateDto.getComment();
    }

    public static Coupon createCoupon(CouponRequestDto.CouponCreate couponCreateDto) {
        return new Coupon(couponCreateDto);
    }

    public void markDeleted() {
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public boolean isAvailable() {
        LocalDateTime now = LocalDateTime.now();
        return !isDeleted()
                && (couponStartAt == null || !now.isBefore(couponStartAt))
                && (couponEndAt == null || !now.isAfter(couponEndAt));
    }

    public int calculateDiscountedPrice(int originalPrice) {
        // 최소 주문 금액 조건 확인
        if (originalPrice < minOrderPrice) {
            return 0;
        }

        int discount;

        switch (couponType) {
            case FIXED:
                discount = discountAmount;
                break;
            case RATE:
                discount = (int) (originalPrice * (discountAmount / 100.0));
                break;
            default:
                throw new IllegalStateException("Unknown coupon type: " + couponType);
        }

        // 최대 할인 금액이 설정되어 있다면 적용
        if (maxDiscountPrice > 0 && discount > maxDiscountPrice) {
            discount = maxDiscountPrice;
        }

        // 원래 금액보다 할인 금액이 클 경우 방지
        return Math.min(discount, originalPrice);
    }

}
