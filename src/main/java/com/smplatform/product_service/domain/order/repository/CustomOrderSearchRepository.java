package com.smplatform.product_service.domain.order.repository;

import com.smplatform.product_service.domain.order.dto.AdminOrderSearchRequestDto;
import com.smplatform.product_service.domain.order.dto.OrderSearchRequestDto;
import com.smplatform.product_service.domain.order.dto.OrderSearchResponseDto;
import com.smplatform.product_service.domain.order.entity.OrderProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CustomOrderSearchRepository {
    Page<OrderSearchResponseDto.MemberOrder> findAllMemberOrderBy(String memberId, OrderSearchRequestDto.MemberOrdersSearch request, Pageable pageable);

    List<OrderProduct> findOrderProductsByOrderIds(List<Long> orderIds);

    Page<OrderSearchResponseDto.AdminOrder> findAllAdminOrderBy(AdminOrderSearchRequestDto.AdminOrdersSearch request, Pageable pageable);
}
