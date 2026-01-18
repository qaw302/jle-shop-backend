package com.smplatform.product_service.domain.coupon.service.impl;

import com.smplatform.product_service.domain.coupon.dto.CouponRequestDto;
import com.smplatform.product_service.domain.coupon.dto.CouponResponseDto;
import com.smplatform.product_service.domain.coupon.entity.Coupon;
import com.smplatform.product_service.domain.coupon.entity.IssueType;
import com.smplatform.product_service.domain.coupon.repository.CouponRepository;
import com.smplatform.product_service.domain.coupon.service.CouponService;
import com.smplatform.product_service.exception.BadRequestException;
import com.smplatform.product_service.exception.InternalServerErrorException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CouponServiceImpl implements CouponService {
    private final CouponRepository couponRepository;

    @Override
    public String createCoupon(CouponRequestDto.CouponCreate couponRequestDto) {
        // 입력값 검증
        if (couponRequestDto == null) {
            throw new BadRequestException("쿠폰 생성 요청 정보가 누락되었습니다.");
        }

        if (couponRequestDto.getIssueType() == IssueType.CODE && (couponRequestDto.getCouponIssueCode() == null || couponRequestDto.getCouponIssueCode().isBlank())) {
            throw new BadRequestException("쿠폰 코드를 입력해주세요.");
        }

        if (couponRequestDto.getCouponName() == null || couponRequestDto.getCouponName().isBlank()) {
            throw new BadRequestException("쿠폰 이름을 입력해주세요.");
        }

        if (couponRequestDto.getAmount() == null || couponRequestDto.getAmount() <= 0) {
            throw new BadRequestException("할인 금액은 0보다 커야 합니다.");
        }

        try {
            Coupon coupon = Coupon.createCoupon(couponRequestDto);
            couponRepository.save(coupon);
            log.info("쿠폰이 성공적으로 생성되었습니다. 쿠폰ID: {}, 쿠폰코드: {}", coupon.getCouponId(), coupon.getCouponCode());
            return String.valueOf(coupon.getCouponId());
        } catch (Exception e) {
            throw new InternalServerErrorException("쿠폰 생성에 실패했습니다.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CouponResponseDto.CouponInfo> getCouponList(CouponRequestDto.CouponSearch couponSearchDto) {
        try {
            if (couponSearchDto == null) {
                log.info("조건 없이 전체 쿠폰 목록 조회");
                return couponRepository.searchCoupon(null).stream()
                        .map(CouponResponseDto.CouponInfo::of)
                        .collect(Collectors.toList());
            }

            log.info("쿠폰 목록 검색 - 검색 조건: {}", couponSearchDto);
            return couponRepository.searchCoupon(couponSearchDto).stream()
                    .map(CouponResponseDto.CouponInfo::of)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new InternalServerErrorException("쿠폰 목록 조회에 실패했습니다.");
        }
    }

    @Override
    public void deleteCoupon(CouponRequestDto.CouponDelete couponRequestDto) {
        if (couponRequestDto == null || couponRequestDto.getCouponId() == null) {
            throw new BadRequestException("삭제할 쿠폰 ID가 누락되었습니다.");
        }

        try {
            Long couponId = couponRequestDto.getCouponId();

            if (!couponRepository.existsById(couponId)) {
                throw new BadRequestException("존재하지 않는 쿠폰입니다.");
            }

            couponRepository.deleteById(couponId);
            log.info("쿠폰이 성공적으로 삭제되었습니다. 쿠폰ID: {}", couponId);

        } catch (InternalServerErrorException e) {
            throw new InternalServerErrorException("쿠폰 삭제 중 비즈니스 로직 오류");
        } catch (Exception e) {
            throw new InternalServerErrorException("쿠폰 삭제에 실패했습니다.");
        }
    }
}
