package com.smplatform.product_service.domain.order.service;


import com.smplatform.product_service.domain.order.dto.PaymentRequestDto;

public interface PaymentService {
    /**
     * 토스 페이먼츠 결제 승인 및 주문 처리
     * 1. 토스 API로 결제 승인
     * 2. 성공 시 주문 상태 업데이트 및 재고 차감
     * 
     * @param dto 결제 승인 요청 DTO (paymentKey, orderNumber, amount)
     * @return 토스 페이먼츠 응답 결과
     */
    String confirmPayment(PaymentRequestDto.PaymentConfirm dto);
}
