package com.smplatform.product_service.domain.product.service;

import com.smplatform.product_service.domain.product.dto.ProductRequestDto;
import com.smplatform.product_service.domain.product.dto.ProductResponseDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {
    ProductResponseDto.ProductGet getProduct(long productId);

    String saveProduct(ProductRequestDto.ProductSave productDto);

    String updateProduct(ProductRequestDto.ProductUpdate productDto);

    String deleteProduct(long productId);

    List<ProductResponseDto.ProductGet> getProducts(ProductRequestDto.AdminProductSearchCondition condition, Pageable pageable);

    List<ProductResponseDto.ProductGetForUsers> getProductsForUsers(int categoryId,
                                                                    ProductRequestDto.UserProductSearchCondition condition,
                                                                    Pageable pageable);

    List<ProductResponseDto.GetTag> getProductsTags();

    List<ProductResponseDto.ProductCategoryMappingGet> getProductCategoryMappings();
}
