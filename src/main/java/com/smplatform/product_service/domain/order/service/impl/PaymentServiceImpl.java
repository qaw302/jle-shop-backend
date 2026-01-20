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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Slf4j
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
    @Transactional
    public String confirmPayment(PaymentRequestDto.PaymentConfirm dto) {
        log.info("결제 승인 시작 - orderNumber: {}, amount: {}", dto.getOrderNumber(), dto.getAmount());
        
        // 1. 주문 조회 및 검증
        Order order = orderRepository.findByOrderNumber(dto.getOrderNumber())
            .orElseThrow(() -> {
                log.error("주문을 찾을 수 없음 - orderNumber: {}", dto.getOrderNumber());
                return new IllegalArgumentException("주문을 찾을 수 없습니다: " + dto.getOrderNumber());
            });
        
        // 2. 멱등성 체크 (이미 결제 완료된 주문인지 확인)
        if (order.getOrderStatus() == OrderStatus.PAYMENT_COMPLETED) {
            log.warn("이미 결제 완료된 주문 - orderNumber: {}", dto.getOrderNumber());
            return createAlreadyCompletedResponse(dto.getOrderNumber());
        }
        
        // 3. 결제 금액 검증
        if (!dto.getAmount().equals(order.getTotalPrice().longValue())) {
            log.error("결제 금액 불일치 - 주문금액: {}, 결제금액: {}", order.getTotalPrice(), dto.getAmount());
            throw new IllegalArgumentException(
                String.format("결제 금액이 일치하지 않습니다. 주문금액: %d, 결제금액: %d", 
                    order.getTotalPrice(), dto.getAmount())
            );
        }
        
        // 4. 토스 페이먼츠 API 결제 승인 요청
        String tossResponse = callTossPaymentsApi(dto);
        log.info("토스 페이먼츠 결제 승인 성공 - orderNumber: {}", dto.getOrderNumber());
        
        // 5. 결제 승인 성공 -> 주문 처리 (상태 변경 + 재고 차감)
        processOrderAfterPayment(order);
        
        log.info("결제 처리 완료 - orderNumber: {}, 최종 상태: {}", 
                 dto.getOrderNumber(), order.getOrderStatus());
        
        return tossResponse;
    }
    
    /**
     * 토스 페이먼츠 API 호출
     */
    private String callTossPaymentsApi(PaymentRequestDto.PaymentConfirm dto) {
        String encodedAuth = Base64.getEncoder()
            .encodeToString((secret + ":").getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(encodedAuth);

        HttpEntity<PaymentRequestDto.PaymentConfirm> entity = new HttpEntity<>(dto, headers);
        String url = "https://api.tosspayments.com/v1/payments/confirm";
        
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("토스 페이먼츠 API 호출 실패 - orderNumber: {}, error: {}", 
                     dto.getOrderNumber(), e.getMessage(), e);
            throw new RuntimeException("결제 승인 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
    
    /**
     * 결제 성공 후 주문 처리
     * - 주문 상태 변경
     * - 주문 상품 상태 변경
     * - 재고 차감
     */
    private void processOrderAfterPayment(Order order) {
        log.info("주문 후처리 시작 - orderId: {}, 현재 상태: {}", 
                 order.getOrderId(), order.getOrderStatus());
        
        // 1. 주문 상태를 결제 완료로 변경
        order.updateStatus(OrderStatus.PAYMENT_COMPLETED);
        log.info("주문 상태 업데이트 - orderId: {}, 새 상태: PAYMENT_COMPLETED", order.getOrderId());

        // 2. 주문 상품 목록 조회
        List<OrderProduct> orderProducts = orderProductRepository.findAllByOrder(order);
        log.info("주문 상품 목록 조회 - orderId: {}, 상품 개수: {}", 
                 order.getOrderId(), orderProducts.size());

        // 3. 각 주문 상품 처리
        for (OrderProduct orderProduct : orderProducts) {
            log.info("주문 상품 처리 - orderProductId: {}, productOptionId: {}, quantity: {}", 
                     orderProduct.getOrderProductId(), 
                     orderProduct.getProductOptionId(), 
                     orderProduct.getQuantity());
            
            // 3-1. 주문 상품 상태를 결제 완료로 변경
            orderProduct.completePayment();
            
            // 3-2. 재고 차감
            ProductOption productOption = productOptionRepository
                .findById(orderProduct.getProductOptionId())
                .orElseThrow(() -> {
                    log.error("상품 옵션을 찾을 수 없음 - productOptionId: {}", 
                             orderProduct.getProductOptionId());
                    return new ProductOptionNotFoundException(
                        "상품 옵션을 찾을 수 없습니다: " + orderProduct.getProductOptionId()
                    );
                });

            int beforeStock = productOption.getStockQuantity();
            productOption.decreaseStock(orderProduct.getQuantity());
            int afterStock = productOption.getStockQuantity();
            
            log.info("재고 차감 완료 - productOptionId: {}, 변경 전: {}, 변경 후: {}", 
                     orderProduct.getProductOptionId(), beforeStock, afterStock);
        }

        // 4. 명시적으로 저장
        orderRepository.save(order);
        orderProductRepository.saveAll(orderProducts);
        
        log.info("주문 후처리 완료 - orderId: {}, 처리된 상품 수: {}", 
                 order.getOrderId(), orderProducts.size());
    }
    
    /**
     * 이미 결제 완료된 주문에 대한 응답 생성
     */
    private String createAlreadyCompletedResponse(String orderNumber) {
        return String.format(
            "{\"status\":\"ALREADY_COMPLETED\",\"message\":\"이미 결제가 완료된 주문입니다.\",\"orderNumber\":\"%s\"}", 
            orderNumber
        );
    }
}
