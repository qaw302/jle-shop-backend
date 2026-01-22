package com.smplatform.product_service.domain.product.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.smplatform.product_service.domain.product.entity.Product;
import com.smplatform.product_service.domain.product.entity.QProduct;
import com.smplatform.product_service.domain.product.entity.QProductCategoryMapping;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CustomProductCategoryRepositoryImpl implements CustomProductCategoryRepository {
    private final JPAQueryFactory queryFactory;
    private QProductCategoryMapping productCategoryMapping = QProductCategoryMapping.productCategoryMapping;
    private QProduct product = QProduct.product;

    @Override
    public List<Product> findAllByCategoryId(int categoryId) {
        return queryFactory.select(product)
                .from(productCategoryMapping)
                .innerJoin(productCategoryMapping.product, product)
                .where(
                        productCategoryMapping.category.categoryId.eq(categoryId),
                        product.isDeleted.eq(false)
                )
                .fetch();
    }
}
