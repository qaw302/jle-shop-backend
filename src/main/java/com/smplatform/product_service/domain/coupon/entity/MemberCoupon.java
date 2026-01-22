package com.smplatform.product_service.domain.coupon.entity;

import com.smplatform.product_service.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "member_coupons",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_member_coupons_member_coupon", columnNames = {"member_id", "coupon_id"})
        }
)
public class MemberCoupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_coupon_id")
    private long memberCouponId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @Column(name = "issued_at")
    private LocalDateTime issuedAt;

    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    @Column
    @Enumerated(EnumType.STRING)
    private MemberCouponStatus status;

    private MemberCoupon(Member member, Coupon coupon) {
        this.member = member;
        this.coupon = coupon;
        this.status = MemberCouponStatus.ACTIVE;
    }

    public static MemberCoupon createMemberCoupon(Member member, Coupon coupon) {
        return new MemberCoupon(member, coupon);
    }

    public void markUsed() {
        this.status = MemberCouponStatus.USED;
    }

    public void markExpired() {
        this.status = MemberCouponStatus.EXPIRED;
    }
}
