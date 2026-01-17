package com.smplatform.product_service.domain.order.controller;

import com.smplatform.product_service.domain.order.dto.OrderRequestDto;
import com.smplatform.product_service.domain.order.dto.OrderResponseDto;
import com.smplatform.product_service.domain.order.dto.OrderSearchRequestDto;
import com.smplatform.product_service.domain.order.dto.OrderSearchResponseDto;
import com.smplatform.product_service.domain.order.service.OrderSearchService;
import com.smplatform.product_service.domain.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Order", description = "Order management APIs")
public class OrderController {
    private final OrderService orderService;
    private final OrderSearchService orderSearchService;

    @PostMapping
    @Operation(summary = "주문 요청", description = "주문시 해당 API를 호출")
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
    @Operation(summary = "주문 수정", description = "주문 수정 기능 (미구현)")
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

    @Operation(summary = "member order list", description = "사용자의 주문내역 조회")
    @PostMapping("/search")
    public OrderSearchResponseDto.MemberOrdersGet searchMemberOrders(
            @RequestHeader("X-MEMBER-ID") String memberId,
            @RequestBody @Valid OrderSearchRequestDto.MemberOrdersSearch request) {
        return orderSearchService.getMemberOrders(memberId, request);
    }
}
