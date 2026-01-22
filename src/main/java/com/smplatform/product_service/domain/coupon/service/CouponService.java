package com.smplatform.product_service.domain.coupon.service;

import com.smplatform.product_service.domain.coupon.dto.CouponRequestDto;
import com.smplatform.product_service.domain.coupon.dto.CouponResponseDto;

import java.util.List;

public interface CouponService {
    String createCoupon(CouponRequestDto.CouponCreate couponRequestDto);

    List<CouponResponseDto.CouponInfo> getCouponList(CouponRequestDto.CouponSearch couponSearchDto);

    void deleteCoupon(CouponRequestDto.CouponDelete couponRequestDto);
}
