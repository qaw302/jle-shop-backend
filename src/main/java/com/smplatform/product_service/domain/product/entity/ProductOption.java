package com.smplatform.product_service.domain.product.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "product_option")
public class ProductOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_option_id")
    private long productOptionId;

    @Column(name = "product_option_name")
    private String productOptionName;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "stock_quantity")
    private int stockQuantity;

    @Column(name = "additional_price")
    private int additionalPrice;

    @Setter
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    public void decreaseStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("차감할 수량은 0보다 커야 합니다.");
        }
        if (this.stockQuantity < quantity) {
            throw new IllegalArgumentException(
                String.format("재고가 부족합니다. 현재 재고: %d, 요청 수량: %d", this.stockQuantity, quantity)
            );
        }
        this.stockQuantity -= quantity;
    }

    public void increaseStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("추가할 수량은 0보다 커야 합니다.");
        }
        this.stockQuantity += quantity;
    }
}