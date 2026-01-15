package com.smplatform.product_service.domain.order.service;


import com.smplatform.product_service.domain.order.dto.PaymentRequestDto;

public interface PaymentService {
    String confirmPayment(PaymentRequestDto.PaymentConfirm dto);
    void processPaymentSuccess(String orderNumber);  // orderId -> orderNumber
}
