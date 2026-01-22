package com.smplatform.product_service.domain.product.repository.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.smplatform.product_service.domain.discount.entity.QDiscount;
import com.smplatform.product_service.domain.product.dto.ProductRequestDto;
import com.smplatform.product_service.domain.product.dto.ProductResponseDto;
import com.smplatform.product_service.domain.product.entity.*;
import com.smplatform.product_service.domain.product.repository.CustomProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.*;

@Slf4j
@RequiredArgsConstructor
public class CustomProductRepositoryImpl implements CustomProductRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<Product> searchProducts(ProductRequestDto.AdminProductSearchCondition condition, Pageable pageable) {
        QProduct product = QProduct.product;
        QProductTag productTag = QProductTag.productTag;
        QTag tag = QTag.tag;
        QDiscount discount = QDiscount.discount;
        QProductCategoryMapping productCategoryMapping = QProductCategoryMapping.productCategoryMapping;

        JPAQuery<Product> query = jpaQueryFactory
                .selectFrom(product)
                .leftJoin(product.discount).fetchJoin()
                .leftJoin(product.productTags, productTag)
                .leftJoin(productTag.tag, tag)
                .leftJoin(productCategoryMapping).on(productCategoryMapping.product.eq(product))
                .where(
                        condition.getCategoryId() != null ? productCategoryMapping.category.categoryId.eq(condition.getCategoryId()) : null,
                        buildCondition(product, tag, discount, condition)
                )
                .distinct();

        List<Product> content = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = query.stream().count();
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<Product> searchUserProducts(int categoryId, ProductRequestDto.UserProductSearchCondition condition, Pageable pageable) {
        QProduct product = QProduct.product;
        QProductTag productTag = QProductTag.productTag;
        QTag tag = QTag.tag;
        QDiscount discount = QDiscount.discount;
        QProductCategoryMapping productCategoryMapping = QProductCategoryMapping.productCategoryMapping;

        JPAQuery<Product> query = jpaQueryFactory
                .selectFrom(product)
                .leftJoin(product.discount).fetchJoin()
                .leftJoin(product.productTags, productTag)
                .leftJoin(productTag.tag, tag)
                .leftJoin(productCategoryMapping).on(productCategoryMapping.product.eq(product))
                .where(
                        categoryId == 0 ? null : productCategoryMapping.category.categoryId.eq(categoryId),
                        product.isDeleted.eq(false),
                        buildCondition(product, tag, discount, condition)
                )
                .distinct();

        List<Product> content = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = query.stream().count();
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public List<ProductResponseDto.ProductOptionGet> findProductOptionsWithDetails(Long productId) {
        QProductOption po = QProductOption.productOption;
        QProductOptionDetail pod = QProductOptionDetail.productOptionDetail;

        List<Tuple> tuples = jpaQueryFactory
                .select(po, pod)
                .from(po)
                .leftJoin(pod).on(pod.productOption.eq(po))
                .where(po.product.productId.eq(productId))
                .fetch();

        Map<Long, ProductResponseDto.ProductOptionGet> optionMap = new LinkedHashMap<>();

        for (Tuple t : tuples) {
            ProductOption productOption = t.get(po);
            if (productOption == null) continue;
            ProductOptionDetail productOptionDetail = t.get(pod);

            ProductResponseDto.ProductOptionGet productOptionGet = optionMap.computeIfAbsent(
                    productOption.getProductOptionId(),
                    key -> ProductResponseDto.ProductOptionGet.of(productOption)
            );

            if (!Objects.isNull(productOptionDetail)) {
                if (Objects.isNull(productOptionGet.getProductOptionDetails())) {
                    productOptionGet.setProductOptionDetails(new ArrayList<>());
                }
                productOptionGet.getProductOptionDetails().add(
                        ProductResponseDto.GetProductOptionDetail.of(productOptionDetail)
                );
            }

        }
        return new ArrayList<>(optionMap.values());
    }

    private Predicate buildCondition(QProduct product, QTag tag, QDiscount discount,
                                     ProductRequestDto.ProductSearchConditions condition) {
        BooleanBuilder builder = new BooleanBuilder();

        if (!Strings.isBlank(condition.getKeyword())) {
            builder.and(product.name.containsIgnoreCase(condition.getKeyword()));
        }

        if (!Strings.isBlank(condition.getTagName())) {
            builder.and(tag.tagName.eq(condition.getTagName()));
        }
        if (!Strings.isBlank(condition.getDiscountName())) {
            builder.and(discount.discountName.eq(condition.getDiscountName()));
        }
        if (condition.getStartDate() != null && condition.getEndDate() != null) {
            builder.and(product.createdAt.between(condition.getStartDate(), condition.getEndDate()));
        }
        if (condition.getMinimumPrice() != null) {
            builder.and(product.price.goe(condition.getMinimumPrice()));
        }
        if (condition.getMinimumPrice() != null &&
                condition.getMaximumPrice() != null &&
                condition.getMaximumPrice() >= condition.getMinimumPrice()) {
            builder.and(product.price.loe(condition.getMaximumPrice()));
        }

        if (condition instanceof ProductRequestDto.AdminProductSearchCondition adminProductSearchCondition) {
            if (adminProductSearchCondition.getIsSelling() != null) {
                builder.and(product.isSelling.eq(adminProductSearchCondition.getIsSelling()));
            }
            if (adminProductSearchCondition.getIsDeleted() != null) {
                builder.and(product.isDeleted.eq(adminProductSearchCondition.getIsDeleted()));
            }
        }

        return builder;
    }

}
