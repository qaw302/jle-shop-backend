package com.smplatform.product_service.domain.product.entity;

import com.smplatform.product_service.domain.discount.entity.Discount;
import com.smplatform.product_service.domain.ProductState;
import com.smplatform.product_service.domain.category.entity.Category;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Builder
@Entity
@Setter
@Getter
@Table(name = "products")
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    @Id
    @Column(name = "product_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long productId;

    @Column(name = "product_name", unique = true)
    private String name;

    @Column(name = "product_description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_deleted")
    private boolean isDeleted;

    @JoinColumn(name = "category_id", nullable = true)
    @ManyToOne(fetch = FetchType.LAZY)
    private Category category;

    @Column(name = "product_state")
    @Enumerated(EnumType.STRING)
    private ProductState productState;

    @Column(name = "is_selling")
    private boolean isSelling;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "price")
    private int price;

    @JoinColumn(name = "discount_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Discount discount;

    @Column(name = "summary_description")
    private String summaryDescription;

    @Column(name = "simple_description")
    private String simpleDescription;

    @Column(name = "thumbnail_path")
    private String thumbnailPath;

    @OneToMany(mappedBy = "product")
    private List<ProductTag> productTags;

    public int getDiscountedPrice() {
        return Objects.nonNull(discount) && discount.isValidDiscount() ?
                discount.calculateDiscountedPrice(price) : price;
    }

    @PrePersist
    public void setCreatedAt() {
        this.createdAt = LocalDateTime.now();
    }

    public void removeDiscount() {
        this.discount = null;
    }

    public void applyDiscount(Discount discount) {
        this.discount = discount;
    }
}
