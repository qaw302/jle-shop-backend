package com.smplatform.product_service.domain.product.repository;

import com.smplatform.product_service.domain.product.entity.ProductCategoryMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductCategoryMappingRepository extends JpaRepository<ProductCategoryMapping, Integer>, CustomProductCategoryRepository {

    List<ProductCategoryMapping> findAllByCategoryCategoryId(int categoryId);

    List<ProductCategoryMapping> findAllByCategoryCategoryIdAndProduct_IsDeletedFalse(int categoryId);
}
