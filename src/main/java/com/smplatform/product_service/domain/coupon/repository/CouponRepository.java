package com.smplatform.product_service.domain.coupon.repository;

import com.smplatform.product_service.domain.coupon.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long>, CustomCouponRepository {
    Optional<Coupon> findByCouponCodeAndDeletedAtIsNull(String couponCode);
}
