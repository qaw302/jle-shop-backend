package com.smplatform.product_service.domain.order.controller;

import com.smplatform.product_service.domain.order.dto.PaymentRequestDto;
import com.smplatform.product_service.domain.order.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payment", description = "Payment management APIs")
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/confirm")
    @Operation(
        summary = "결제 승인 및 주문 처리", 
        description = "토스 페이먼츠 결제 승인 후 자동으로 주문 상태 업데이트 및 재고 차감을 수행합니다."
    )
    public ResponseEntity<String> confirmPayment(@RequestBody PaymentRequestDto.PaymentConfirm dto) {
        log.info("결제 승인 요청 - paymentKey: {}, orderNumber: {}, amount: {}", 
                 dto.getPaymentKey(), dto.getOrderNumber(), dto.getAmount());
        
        try {
            String result = paymentService.confirmPayment(dto);
            
            log.info("결제 승인 및 주문 처리 완료 - orderNumber: {}", dto.getOrderNumber());
            return ResponseEntity.ok(result);
            
        } catch (IllegalArgumentException e) {
            // 비즈니스 로직 예외 (주문 없음, 금액 불일치 등)
            log.error("결제 승인 실패 - orderNumber: {}, error: {}", 
                     dto.getOrderNumber(), e.getMessage());
            return ResponseEntity.badRequest().body(
                String.format("{\"error\":\"%s\"}", e.getMessage())
            );
            
        } catch (Exception e) {
            // 시스템 예외 (토스 API 오류 등)
            log.error("결제 처리 중 시스템 오류 - orderNumber: {}, error: {}", 
                     dto.getOrderNumber(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                String.format("{\"error\":\"결제 처리 중 오류가 발생했습니다: %s\"}", e.getMessage())
            );
        }
    }

}
