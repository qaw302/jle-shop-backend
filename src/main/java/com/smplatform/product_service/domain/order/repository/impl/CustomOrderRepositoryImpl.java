package com.smplatform.product_service.domain.order.repository.impl;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.smplatform.product_service.domain.discount.entity.QDiscount;
import com.smplatform.product_service.domain.order.dto.OrderResponseDto;
import com.smplatform.product_service.domain.order.repository.CustomOrderRepository;
import com.smplatform.product_service.domain.product.entity.QProduct;
import com.smplatform.product_service.domain.product.entity.QProductOption;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class CustomOrderRepositoryImpl implements CustomOrderRepository {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<OrderResponseDto.ProductOptionFlatDto> findAllByProductOptionIds(List<Long> productOptionIds) {
        QProductOption productOption = QProductOption.productOption;
        QProduct product = QProduct.product;
        QDiscount discount = QDiscount.discount;

        return jpaQueryFactory.select(Projections.constructor(OrderResponseDto.ProductOptionFlatDto.class,
                productOption.productOptionId,
                productOption.productOptionName,
                productOption.stockQuantity,
                productOption.additionalPrice,

                product.productId,
                product.name,
                product.isDeleted,
                product.isSelling,
                product.price,

                discount.discountId,
                discount.discountType,
                discount.discountValue,
                discount.discountStartDate,
                discount.discountEndDate))
                .from(productOption)
                .join(productOption.product, product)
                .leftJoin(product.discount, discount)
                .where(productOption.productOptionId.in(productOptionIds))
                .fetch();
    }
}
