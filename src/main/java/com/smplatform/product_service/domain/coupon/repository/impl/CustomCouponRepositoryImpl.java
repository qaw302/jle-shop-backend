package com.smplatform.product_service.domain.coupon.repository.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.smplatform.product_service.domain.coupon.dto.CouponRequestDto;
import com.smplatform.product_service.domain.coupon.entity.Coupon;
import com.smplatform.product_service.domain.coupon.entity.QCoupon;
import com.smplatform.product_service.domain.coupon.repository.CustomCouponRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
public class CustomCouponRepositoryImpl implements CustomCouponRepository {
    private final JPAQueryFactory query;
    private final QCoupon coupon = QCoupon.coupon;

    @Override
    public List<Coupon> searchCoupon(CouponRequestDto.CouponSearch couponSearchDto) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        booleanBuilder.and(coupon.deletedAt.isNull());
        if (couponSearchDto == null) {
            return query.selectFrom(coupon)
                    .where(booleanBuilder)
                    .fetch();
        }
        if (Objects.nonNull(couponSearchDto.getCouponName())) {
            booleanBuilder.and(coupon.couponName.contains(couponSearchDto.getCouponName()));
        }
        if (Objects.nonNull(couponSearchDto.getCouponStartDate())) {
            booleanBuilder.and(coupon.couponStartAt.goe(couponSearchDto.getCouponStartDate()));
        }
        if (Objects.nonNull(couponSearchDto.getCouponEndDate())) {
            booleanBuilder.and(coupon.couponEndAt.loe(couponSearchDto.getCouponEndDate()));
        }
        return query.selectFrom(coupon)
                .where(booleanBuilder)
                .fetch();
    }
}
