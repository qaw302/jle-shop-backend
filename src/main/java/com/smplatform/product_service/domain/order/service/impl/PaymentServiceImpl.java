package com.smplatform.product_service.domain.order.service.impl;

import com.smplatform.product_service.domain.order.dto.PaymentRequestDto;
import com.smplatform.product_service.domain.order.entity.Order;
import com.smplatform.product_service.domain.order.entity.OrderProduct;
import com.smplatform.product_service.domain.order.entity.OrderStatus;
import com.smplatform.product_service.domain.order.repository.OrderProductRepository;
import com.smplatform.product_service.domain.order.repository.OrderRepository;
import com.smplatform.product_service.domain.order.service.PaymentService;
import com.smplatform.product_service.domain.product.entity.ProductOption;
import com.smplatform.product_service.domain.product.exception.ProductOptionNotFoundException;
import com.smplatform.product_service.domain.product.repository.ProductOptionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final RestTemplate restTemplate;
    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final ProductOptionRepository productOptionRepository;

    @Value("${toss.api.secret}")
    private String secret;

    @Override
    public String confirmPayment(PaymentRequestDto.PaymentConfirm dto) {
        String encodedAuth = Base64.getEncoder().encodeToString((secret + ":").getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(encodedAuth);

        HttpEntity<PaymentRequestDto.PaymentConfirm> entity = new HttpEntity<>(dto, headers);

        String url = "https://api.tosspayments.com/v1/payments/confirm";
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        return response.getBody();
    }

    @Override
    @Transactional
    public void processPaymentSuccess(String orderNumber) {
        // 주문 조회 (orderNumber로 조회)
        Order order = orderRepository.findByOrderNumber(orderNumber)
            .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderNumber));

        // 주문 상태를 완료로 변경
        order.completeOrder();

        // 주문 상품 목록 조회
        List<OrderProduct> orderProducts = orderProductRepository.findAllByOrder(order);

        // 각 주문 상품 처리
        for (OrderProduct orderProduct : orderProducts) {
            // 주문 상품 상태를 결제 완료로 변경
            orderProduct.completePayment();

            // 재고 차감
            ProductOption productOption = productOptionRepository.findById(orderProduct.getProductOptionId())
                .orElseThrow(() -> new ProductOptionNotFoundException(
                    "상품 옵션을 찾을 수 없습니다: " + orderProduct.getProductOptionId()
                ));

            productOption.decreaseStock(orderProduct.getQuantity());
        }

        // 엔티티 변경사항은 트랜잭션 커밋 시 자동으로 저장됨 (Dirty Checking)
    }
}
