package com.smplatform.product_service.domain.coupon.repository.impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.smplatform.product_service.domain.coupon.dto.MemberCouponResponseDto;
import com.smplatform.product_service.domain.coupon.dto.QMemberCouponResponseDto_MemberCouponInfo;
import com.smplatform.product_service.domain.coupon.entity.QCoupon;
import com.smplatform.product_service.domain.coupon.entity.QMemberCoupon;
import com.smplatform.product_service.domain.coupon.repository.CustomMemberCouponRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class CustomMemberCouponRepositoryImpl implements CustomMemberCouponRepository {
    private final JPAQueryFactory query;
    private final QCoupon c = QCoupon.coupon;
    private final QMemberCoupon mc = QMemberCoupon.memberCoupon;

    @Override
    public List<MemberCouponResponseDto.MemberCouponInfo> findAllByMemberId(String memberId) {

        return query
                .select(new QMemberCouponResponseDto_MemberCouponInfo(
                        mc.memberCouponId,
                        c.couponCode,
                        c.couponName,
                        c.couponType.stringValue(),
                        c.discountAmount,
                        c.minOrderPrice,
                        c.maxDiscountPrice,
                        mc.issuedAt,
                        c.couponStartAt,
                        c.couponEndAt,
                        c.comment,
                        mc.status.stringValue(),
                        c.couponId))
                .from(mc)
                .join(mc.coupon, c)
                .where(mc.member.memberId.eq(memberId)
                        .and(c.deletedAt.isNull()))
                .fetch()
                ;
    }
}
