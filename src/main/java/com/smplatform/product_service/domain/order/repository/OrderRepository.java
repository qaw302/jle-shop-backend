package com.smplatform.product_service.domain.order.repository;

import com.smplatform.product_service.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long>, CustomOrderRepository, CustomOrderSearchRepository {
    Optional<Order> findByOrderNumber(String orderNumber);
}
