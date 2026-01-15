package com.smplatform.product_service.domain.order.service.impl;

import com.smplatform.product_service.domain.order.dto.OrderSearchRequestDto;
import com.smplatform.product_service.domain.order.dto.OrderSearchResponseDto;
import com.smplatform.product_service.domain.order.entity.OrderProduct;
import com.smplatform.product_service.domain.order.repository.OrderRepository;
import com.smplatform.product_service.domain.order.service.OrderSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class OrderSearchServiceImpl implements OrderSearchService {
    private final OrderRepository orderRepository;

    @Override
    public OrderSearchResponseDto.MemberOrdersGet getMemberOrders(String memberId, OrderSearchRequestDto.MemberOrdersSearch request) {
        // PageableDto로 Pageable 객체 생성
        Pageable pageable = PageRequest.of(
                request.getPageable().getPage(),
                request.getPageable().getSize(),
                Sort.by(request.getPageable().getOrders())
        );
        // response의 "content" 생성
        Page<OrderSearchResponseDto.MemberOrder> resultPage = orderRepository.findAllMemberOrderBy(memberId, request, pageable);

        // "content"의 "products" 생성 & 할당
        List<Long> orderIds = resultPage.getContent().stream().map(OrderSearchResponseDto.MemberOrder::getOrderId).toList();
        List<OrderProduct> orderProducts = orderRepository.findOrderProductsByOrderIds(orderIds);

        Map<Long, List<OrderProduct>> orderProductMap = orderProducts.stream()
                .collect(Collectors.groupingBy(orderProduct -> orderProduct.getOrder().getOrderId()));
        resultPage.getContent().forEach(order -> order.setProducts(
                orderProductMap.getOrDefault(order.getOrderId(), List.of()).stream()
                        .map(OrderSearchResponseDto.OrderProductDto::of)
                        .toList()
        ));

        return new OrderSearchResponseDto.MemberOrdersGet(
                resultPage.getContent(),
                resultPage.getNumber(),
                resultPage.getSize(),
                resultPage.getTotalElements(),
                resultPage.getTotalPages()
        );
    }
}
