package com.smplatform.product_service.domain.order.controller;

import com.smplatform.product_service.annotation.AdminOnly;
import com.smplatform.product_service.domain.order.dto.AdminOrderSearchRequestDto;
import com.smplatform.product_service.domain.order.dto.OrderSearchResponseDto;
import com.smplatform.product_service.domain.order.service.OrderSearchService;
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

    @AdminOnly
    @PostMapping("/search")
    @Operation(summary = "관리자 주문 전체 조회", description = "관리자가 조건에 따른 주문내역을 조회합니다.")
    public ResponseEntity<OrderSearchResponseDto.AdminOrdersGet> searchAdminOrders(
            @RequestBody @Valid AdminOrderSearchRequestDto.AdminOrdersSearch request) {
        return ResponseEntity.ok(orderSearchService.getAdminOrders(request));
    }
}
