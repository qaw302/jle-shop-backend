package com.smplatform.product_service.domain.order.dto;

import com.smplatform.product_service.domain.order.entity.OrderProductStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class OrderSearchRequestDto {

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @Schema(name = "MemberOrdersSearchRequest", description = "회원 주문내역 검색 요청")
    public static class MemberOrdersSearch {
        @Schema(description = "검색 조건")
        private SearchConditionsDto conditions;
        @Schema(description = "페이징 정보")
        private PageableDto pageable;
    }

    @Getter
    @AllArgsConstructor
    @Schema(name = "OrderSearchConditionsRequest", description = "주문 검색 조건")
    public static class SearchConditionsDto {
        @Schema(description = "검색 타입", example = "ALL")
        private OrderSearchType type = OrderSearchType.ALL;
        @Schema(description = "주문 상품 상태")
        private OrderProductStatus status = null;
        @Schema(description = "조회 시작일", example = "2024-01-01")
        private LocalDate startDate = LocalDate.now().minusWeeks(1);
        @Schema(description = "조회 종료일", example = "2024-01-31")
        private LocalDate endDate = LocalDate.now();
    }

    public enum OrderSearchType {
        ALL(null),
        NORMAL(OrderProductStatus.StatusType.NORMAL),
        SHIPPING(OrderProductStatus.StatusType.SHIPPING),
        CANCEL_RETURN_EXCHANGE(OrderProductStatus.StatusType.CANCEL_RETURN_EXCHANGE);

        private final OrderProductStatus.StatusType type;

        OrderSearchType(OrderProductStatus.StatusType type) {
            this.type = type;
        }

        public EnumSet<OrderProductStatus> getTargets() {
            return type==null? EnumSet.noneOf(OrderProductStatus.class)
                    : OrderProductStatus.ofType(type);
        }
    }

    @Getter
    @Schema(name = "OrderSearchPageableRequest", description = "페이징 요청")
    public static class PageableDto {
        @Min(value = 0, message = "page 값은 0 이상이어야 합니다")
        @Schema(description = "페이지 번호 (0부터 시작)", example = "0")
        private int page = 0;
        @Min(value = 1)
        @Max(value = 100)
        @Schema(description = "페이지 크기", example = "10")
        private int size = 10;
        @Schema(description = "정렬 정보")
        private SortDto sort;

        public Sort.Direction getDirection() {
            return Optional.ofNullable(sort)
                    .map(SortDto::getDirection)
                    .flatMap(Sort.Direction::fromOptionalString)
                    .orElse(Sort.Direction.DESC);
        }
        public List<Sort.Order> getOrders() {
            return Optional.ofNullable(sort)
                    .map(SortDto::getProperties)
                    .orElse(List.of("orderDate"))
                    .stream().map(prop -> new Sort.Order(getDirection(), prop))
                    .collect(Collectors.toList());
        }
    }

    @Getter
    @Schema(name = "OrderSearchSortRequest", description = "정렬 요청")
    public static class SortDto {
        @Schema(description = "정렬 방향", example = "DESC")
        private String direction;
        @Schema(description = "정렬 필드 목록", example = "[\"orderDate\"]")
        private List<String> properties;
    }
}
