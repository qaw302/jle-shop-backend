package com.smplatform.product_service.domain.product.repository;

import com.smplatform.product_service.domain.discount.entity.Discount;
import com.smplatform.product_service.domain.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, CustomProductRepository {
    @EntityGraph(attributePaths = {"discount"})
    Page<Product> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"discount"})
    Optional<Product> findByProductId(Long productId);
    
    List<Product> findAllByDiscount(Discount discount);
}
