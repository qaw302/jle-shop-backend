package com.smplatform.product_service.domain.order.service;

import com.smplatform.product_service.domain.order.dto.AdminOrderSearchRequestDto;
import com.smplatform.product_service.domain.order.dto.OrderSearchRequestDto;
import com.smplatform.product_service.domain.order.dto.OrderSearchResponseDto;

import java.util.List;

public interface OrderSearchService {
    OrderSearchResponseDto.MemberOrdersGet getMemberOrders(String memberId, OrderSearchRequestDto.MemberOrdersSearch request);

    OrderSearchResponseDto.AdminOrdersGet getAdminOrders(AdminOrderSearchRequestDto.AdminOrdersSearch request);
}

