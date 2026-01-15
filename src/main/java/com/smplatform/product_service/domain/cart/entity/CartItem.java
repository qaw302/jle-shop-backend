package com.smplatform.product_service.domain.cart.entity;

import com.smplatform.product_service.domain.member.entity.Member;
import com.smplatform.product_service.domain.product.entity.ProductOption;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cartItems",
        uniqueConstraints = @UniqueConstraint(name="uq_cart_member_option", columnNames = {"member_id", "product_option_id"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class CartItem {
    @Id
    @Column(name = "cart_item_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long cartItemId;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_option_id")
    private ProductOption productOption;

    @Column
    private int quantity;

    private CartItem(Member member, ProductOption productOption, int quantity) {
        this.member = member;
        this.productOption = productOption;
        this.quantity = quantity;
    }

    public static CartItem createCartItem(Member member, ProductOption productOption, int quantity) {
        return new CartItem(member, productOption, quantity);
    }

    public void updateOption(ProductOption productOption, int quantity) {
        this.productOption = productOption;
        this.quantity = quantity;
    }

    public void addQuantity(int quantity) {
        this.quantity+=quantity;
    }
}
