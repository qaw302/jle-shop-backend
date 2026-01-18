package com.smplatform.product_service.domain.order.service;

import com.smplatform.product_service.domain.order.dto.AdminOrderRequestDto;
import com.smplatform.product_service.domain.order.dto.OrderRequestDto;
import com.smplatform.product_service.domain.order.dto.OrderResponseDto;

public interface OrderService {
    OrderResponseDto.OrderSaveSuccess saveOrder(String memberId, OrderRequestDto.OrderSave requestDto);  // Long -> String
    OrderResponseDto.OrderDetail getOrderDetail(String memberId, Long orderId);
    void cancelOrder(String memberId, Long orderId);
    OrderResponseDto.OrderDetail getOrderDetailByOrderNumber(String memberId, String orderNumber);
    void updateOrderStatus(AdminOrderRequestDto.UpdateStatus request);
    OrderResponseDto.OrderDetail getAdminOrderDetail(Long orderId);
}
