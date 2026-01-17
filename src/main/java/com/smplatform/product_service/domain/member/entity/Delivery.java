package com.smplatform.product_service.domain.member.entity;

import com.smplatform.product_service.domain.order.entity.Order;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Delivery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long deliveryId;

    private String deliveryCode;

    private String deliveryCompany;

    private Integer shippingFee;

    private String postalCode;

    private String address1;
    private String address2;
    private String recipient;
    private String phoneNumber;
}
