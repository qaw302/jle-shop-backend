package com.smplatform.product_service.domain.order.entity;

import com.smplatform.product_service.domain.coupon.entity.MemberCoupon;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_benefits")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OrderBenefit {
    @Id
    private Long orderId;

    @MapsId
    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "point_discount")
    private Integer pointDiscount;
    @Column(name = "coupon_discount")
    private Integer couponDiscount;
    @OneToOne
    @JoinColumn(name = "member_coupon_id")
    private MemberCoupon coupon;
    @Column(name = "total_benefit")
    private Long totalBenefit;
    
    @Column(name = "product_original_total_price")
    private Integer productOriginalTotalPrice;
    
    @Column(name = "product_total_discount_amount")
    private Integer productTotalDiscountAmount;
}
