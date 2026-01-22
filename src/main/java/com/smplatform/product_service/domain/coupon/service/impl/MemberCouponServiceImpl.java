package com.smplatform.product_service.domain.coupon.service.impl;

import com.smplatform.product_service.domain.coupon.dto.MemberCouponRequestDto;
import com.smplatform.product_service.domain.coupon.dto.MemberCouponResponseDto;
import com.smplatform.product_service.domain.coupon.entity.Coupon;
import com.smplatform.product_service.domain.coupon.entity.MemberCoupon;
import com.smplatform.product_service.domain.coupon.entity.MemberCouponStatus;
import com.smplatform.product_service.domain.coupon.exception.CouponNotFoundException;
import com.smplatform.product_service.domain.coupon.repository.CouponRepository;
import com.smplatform.product_service.domain.coupon.repository.MemberCouponRepository;
import com.smplatform.product_service.domain.coupon.service.MemberCouponService;
import com.smplatform.product_service.domain.member.entity.Member;
import com.smplatform.product_service.domain.member.exception.MemberNotFoundException;
import com.smplatform.product_service.domain.member.repository.MemberRepository;
import com.smplatform.product_service.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberCouponServiceImpl implements MemberCouponService {
    private final MemberRepository memberRepository ;
    private final CouponRepository couponRepository ;
    private final MemberCouponRepository memberCouponRepository ;

    @Override
    public MemberCouponResponseDto.CouponIssue issueCoupon(String memberId, MemberCouponRequestDto.CouponIssue couponIssueDto) {
        Member member = memberRepository.findById(memberId).orElseThrow(()->new MemberNotFoundException(memberId));
        Coupon coupon = couponRepository.findByCouponCodeAndDeletedAtIsNull(couponIssueDto.getCouponIssueCode())
                .orElseThrow(()-> new CouponNotFoundException(couponIssueDto.getCouponIssueCode()));
        if (memberCouponRepository.existsByMemberMemberIdAndCouponCouponId(memberId, coupon.getCouponId())) {
            throw new BadRequestException("이미 발급된 쿠폰입니다.");
        }

        MemberCoupon memberCoupon = MemberCoupon.createMemberCoupon(member, coupon);
        memberCouponRepository.save(memberCoupon);

        return MemberCouponResponseDto.CouponIssue.of(memberCoupon);
    }

    @Override
    public List<MemberCouponResponseDto.MemberCouponInfo> getCoupons(String memberId) {
        memberRepository.findById(memberId).orElseThrow(()->new MemberNotFoundException(memberId));
        List<MemberCoupon> activeCoupons = memberCouponRepository
                .findAllByMemberMemberIdAndStatus(memberId, MemberCouponStatus.ACTIVE);
        LocalDate today = LocalDate.now();
        activeCoupons.forEach(memberCoupon -> {
            LocalDate endAt = memberCoupon.getCoupon().getCouponEndAt();
            if (endAt != null && endAt.isBefore(today)) {
                memberCoupon.markExpired();
            }
        });
        return memberCouponRepository.findAllByMemberId(memberId);
    }
}
