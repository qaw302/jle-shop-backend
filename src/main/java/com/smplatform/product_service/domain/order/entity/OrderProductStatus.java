package com.smplatform.product_service.domain.order.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public enum OrderProductStatus {
    PAYMENT_PENDING("결제대기", StatusType.NORMAL),
    PAYMENT_COMPLETED("결제완료", StatusType.NORMAL),
    PREPARING("배송준비중",  StatusType.SHIPPING),
    SHIPPING("배송중", StatusType.SHIPPING),
    DELIVERED("배송완료", StatusType.SHIPPING),
    PAYMENT_FAILED("결제실패", StatusType.NORMAL),

    ORDER_CANCELLED("주문취소", StatusType.CANCEL_RETURN_EXCHANGE),
    CANCEL_COMPLETED("주문취소완료", StatusType.CANCEL_RETURN_EXCHANGE),
    RETURN_REQUEST("반품요청", StatusType.CANCEL_RETURN_EXCHANGE),
    RETURN_COMPLETED("반품완료", StatusType.CANCEL_RETURN_EXCHANGE),
    EXCHANGE_REQUEST("교환요청", StatusType.CANCEL_RETURN_EXCHANGE),
    EXCHANGE_COMPLETED("교환완료", StatusType.CANCEL_RETURN_EXCHANGE),
    PICKUP_IN_PROGRESS("수거중", StatusType.CANCEL_RETURN_EXCHANGE),
    PICKED_UP("수거완료", StatusType.CANCEL_RETURN_EXCHANGE);

    private final String label;
    private final StatusType type;

    public enum StatusType {
        NORMAL, SHIPPING, CANCEL_RETURN_EXCHANGE
    }

    private static final Map<StatusType, EnumSet<OrderProductStatus>> STATUS_MAP = Arrays.stream(values()).collect(Collectors.groupingBy(
            OrderProductStatus::getType,
            Collectors.collectingAndThen(Collectors.toSet(), EnumSet::copyOf)
    ));

    public static EnumSet<OrderProductStatus> ofType(StatusType type) {
        return STATUS_MAP.getOrDefault(type, EnumSet.noneOf(OrderProductStatus.class));
    }
}
