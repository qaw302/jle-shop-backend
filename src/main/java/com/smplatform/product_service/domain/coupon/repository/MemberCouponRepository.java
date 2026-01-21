package com.smplatform.product_service.domain.coupon.repository;

import com.smplatform.product_service.domain.coupon.entity.MemberCoupon;
import com.smplatform.product_service.domain.coupon.entity.MemberCouponStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberCouponRepository extends JpaRepository<MemberCoupon, Long>, CustomMemberCouponRepository {
    List<MemberCoupon> findAllByMemberMemberIdAndStatus(String memberId, MemberCouponStatus status);
}
