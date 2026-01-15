package com.smplatform.product_service.domain.order.controller;

import com.smplatform.product_service.domain.order.dto.OrderRequestDto;
import com.smplatform.product_service.domain.order.dto.OrderResponseDto;
import com.smplatform.product_service.domain.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "주문 결제시 호출하는 API", description = "주문 완료시 해당 API를 호출, cartItemId는 Nullable")
    public ResponseEntity<OrderResponseDto.OrderSaveSuccess> saveOrder(@RequestHeader(name = "X-MEMBER-ID") String id,
                                                                       @RequestBody OrderRequestDto.OrderSave requestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.saveOrder(id, requestDto));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "주문 상세 조회", description = "주문 ID로 주문 상세 정보를 조회합니다")
    public ResponseEntity<OrderResponseDto.OrderDetail> getOrder(
            @RequestHeader(name = "X-MEMBER-ID") String memberId,
            @Parameter(description = "주문 ID") @PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrderDetail(memberId, orderId));
    }

    @PutMapping
    public ResponseEntity<?> updateOrder() {
        return null;
    }

    @DeleteMapping("/{orderId}")
    @Operation(summary = "주문 취소", description = "주문을 취소하고 재고를 복구합니다")
    public ResponseEntity<String> cancelOrder(
            @RequestHeader(name = "X-MEMBER-ID") String memberId,
            @Parameter(description = "주문 ID") @PathVariable Long orderId) {
        orderService.cancelOrder(memberId, orderId);
        return ResponseEntity.ok("주문이 취소되었습니다.");
    }
}
