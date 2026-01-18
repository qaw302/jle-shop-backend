package com.smplatform.product_service.domain.order.controller;

import com.smplatform.product_service.annotation.AdminOnly;
import com.smplatform.product_service.domain.order.dto.AdminOrderRequestDto;
import com.smplatform.product_service.domain.order.dto.AdminOrderSearchRequestDto;
import com.smplatform.product_service.domain.order.dto.OrderSearchResponseDto;
import com.smplatform.product_service.domain.order.service.OrderSearchService;
import com.smplatform.product_service.domain.order.service.OrderService;
import com.smplatform.product_service.domain.order.dto.OrderResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/admin/orders")
@RequiredArgsConstructor
@Tag(name = "Admin Order", description = "Admin Order management APIs")
public class AdminOrderController {
    private final OrderSearchService orderSearchService;
    private final OrderService orderService;

    @AdminOnly
    @PostMapping("/search")
    @Operation(summary = "관리자 주문 전체 조회", description = "관리자가 조건에 따른 주문내역을 조회합니다.")
    public ResponseEntity<OrderSearchResponseDto.AdminOrdersGet> searchAdminOrders(
            @RequestBody @Valid AdminOrderSearchRequestDto.AdminOrdersSearch request) {
        return ResponseEntity.ok(orderSearchService.getAdminOrders(request));
    }

    @AdminOnly
    @PostMapping("/status")
    @Operation(summary = "관리자 주문 상태 수정", description = "관리자가 주문의 상태를 수정합니다.")
    public ResponseEntity<Void> updateOrderStatus(@RequestBody @Valid AdminOrderRequestDto.UpdateStatus request) {
        orderService.updateOrderStatus(request);
        return ResponseEntity.ok().build();
    }

    @AdminOnly
    @GetMapping("/{orderId}")
    @Operation(summary = "관리자 주문 상세 조회", description = "관리자가 주문 상세 정보를 조회합니다.")
    public ResponseEntity<OrderResponseDto.OrderDetail> getAdminOrderDetail(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getAdminOrderDetail(orderId));
    }
}
