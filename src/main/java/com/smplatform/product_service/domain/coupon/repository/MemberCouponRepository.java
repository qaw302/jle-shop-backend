package com.smplatform.product_service.domain.coupon.repository;

import com.smplatform.product_service.domain.coupon.entity.MemberCoupon;
import com.smplatform.product_service.domain.coupon.entity.MemberCouponStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MemberCouponRepository extends JpaRepository<MemberCoupon, Long>, CustomMemberCouponRepository {
    List<MemberCoupon> findAllByMemberMemberIdAndStatus(String memberId, MemberCouponStatus status);
    boolean existsByMemberMemberIdAndCouponCouponId(String memberId, Long couponId);

    @Modifying
    @Query("update MemberCoupon mc set mc.status = :status " +
            "where mc.coupon.couponId = :couponId and mc.status = :targetStatus")
    int updateStatusByCouponId(@Param("couponId") Long couponId,
                               @Param("targetStatus") MemberCouponStatus targetStatus,
                               @Param("status") MemberCouponStatus status);
}
