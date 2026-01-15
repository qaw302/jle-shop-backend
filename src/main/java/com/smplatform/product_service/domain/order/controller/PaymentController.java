package com.smplatform.product_service.domain.order.controller;

import com.smplatform.product_service.domain.order.dto.PaymentRequestDto;
import com.smplatform.product_service.domain.order.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payment", description = "Payment management APIs")
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/confirm")
    @Operation(summary = "결제 승인", description = "결제 승인 후 주문 상태 업데이트 및 재고 차감을 수행합니다")
    public ResponseEntity<String> confirmPayment(@RequestBody PaymentRequestDto.PaymentConfirm dto) {
        String result = paymentService.confirmPayment(dto);
        
        // 결제 승인 성공 시 후처리 (주문 상태 업데이트, 재고 차감)
        try {
            paymentService.processPaymentSuccess(dto.getOrderId());
        } catch (Exception e) {
            // 후처리 실패 시 로깅 (결제는 이미 완료되었으므로 예외를 던지지 않음)
            System.err.println("결제 후처리 실패: " + e.getMessage());
        }
        
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/success")
    @Operation(summary = "결제 성공 처리", description = "결제 성공 후처리를 수행합니다")
    public ResponseEntity<String> paymentSuccess(@RequestParam String orderNumber) {
        try {
            paymentService.processPaymentSuccess(orderNumber);
            return ResponseEntity.ok("결제가 성공적으로 완료되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("결제 후처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // ========== 테스트용 엔드포인트 ==========

    @PostMapping("/test/simulate-success/{orderId}")
    @Operation(summary = "[TEST] 결제 성공 시뮬레이션", 
               description = "실제 토스 API 호출 없이 결제 완료 후처리만 테스트합니다")
    public ResponseEntity<Map<String, Object>> simulatePaymentSuccess(@PathVariable String orderId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 결제 승인 후처리만 실행 (주문 상태 업데이트, 재고 차감)
            paymentService.processPaymentSuccess(orderId);
            
            response.put("success", true);
            response.put("message", "결제 후처리가 완료되었습니다");
            response.put("orderId", orderId);
            response.put("details", Map.of(
                "orderStatus", "COMPLETE",
                "orderProductStatus", "PAID",
                "stockDecreased", true
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "결제 후처리 실패: " + e.getMessage());
            response.put("orderId", orderId);
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/test/guide")
    @Operation(summary = "[TEST] 토스 페이먼츠 테스트 가이드")
    public ResponseEntity<Map<String, Object>> getTestGuide() {
        Map<String, Object> guide = new HashMap<>();
        
        guide.put("testFlow", Map.of(
            "step1", "POST /v1/orders - 주문 생성 (orderId 획득)",
            "step2", "POST /v1/payments/test/simulate-success/{orderId} - 결제 성공 시뮬레이션",
            "step3", "GET /v1/orders/{orderId} - 주문 상세 조회 (상태 확인)"
        ));
        
        guide.put("realPaymentFlow", Map.of(
            "note", "실제 프로덕션 결제 플로우",
            "step1", "프론트엔드에서 토스 결제 위젯 실행",
            "step2", "사용자가 카드 정보 입력 및 결제",
            "step3", "토스가 paymentKey, orderId, amount 반환",
            "step4", "POST /v1/payments/confirm 호출",
            "step5", "토스 API로 결제 승인 요청",
            "step6", "성공 시 주문 상태 업데이트 및 재고 차감"
        ));
        
        guide.put("tossTestCards", Map.of(
            "카드번호", "4330123456789012 (토스 테스트 카드)",
            "유효기간", "12/25",
            "CVC", "123",
            "비밀번호", "1234",
            "생년월일", "990101",
            "note", "실제 토스 위젯을 사용할 때 이 정보로 테스트 가능"
        ));
        
        guide.put("endpoints", Map.of(
            "testSimulation", "POST /v1/payments/test/simulate-success/{orderId}",
            "realPayment", "POST /v1/payments/confirm",
            "orderDetail", "GET /v1/orders/{orderId}",
            "orderCancel", "DELETE /v1/orders/{orderId}"
        ));
        
        return ResponseEntity.ok(guide);
    }
}
