package com.smplatform.product_service.domain.order.entity;

import com.smplatform.product_service.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "orders")
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @Column(name = "order_number", unique = true, nullable = false)
    private String orderNumber;  // 토스 페이먼츠용 주문번호

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "order_date")
    private LocalDateTime orderDate;

    @Column(name = "total_price")
    private Integer totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status")
    private OrderStatus orderStatus;

    @Column(name = "order_title")
    private String orderTitle;

    @PrePersist
    public void setOrderDate() {
        this.orderDate = LocalDateTime.now();
        if (this.orderNumber == null) {
            this.orderNumber = generateOrderNumber();
        }
    }

    private String generateOrderNumber() {
        // 주문번호 생성: ORD + YYYYMMDD + HHmmss + 랜덤3자리
        // 예: ORD20250115143025001
        LocalDateTime now = LocalDateTime.now();
        String datePart = String.format("%04d%02d%02d%02d%02d%02d",
            now.getYear(), now.getMonthValue(), now.getDayOfMonth(),
            now.getHour(), now.getMinute(), now.getSecond());
        String randomPart = String.format("%03d", (int) (Math.random() * 1000));
        return "ORD" + datePart + randomPart;
    }

    public void updateStatus(OrderStatus newStatus) {
        this.orderStatus = newStatus;
    }

    public void completeOrder() {
        this.orderStatus = OrderStatus.COMPLETE;
    }

    public void cancelOrder() {
        this.orderStatus = OrderStatus.CANCEL;
    }
}
