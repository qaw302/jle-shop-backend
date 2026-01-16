package com.smplatform.product_service.domain.product.dto;

import com.smplatform.product_service.domain.ProductState;
import com.smplatform.product_service.domain.discount.dto.DiscountResponseDto;
import com.smplatform.product_service.domain.product.entity.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class ProductResponseDto {

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RegisterProduct {
        long productId;
    }

    @Builder
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductGet {
        long productId;
        private String name;
        private String description;
        private boolean isDeleted;
        private ProductState productState;
        private boolean isSelling;
        private LocalDateTime createdAt;
        private int price;
        private Long discountId;
        private String summaryDescription;
        private String simpleDescription;
        private int discountedPrice;
        private List<ProductOptionGet> productOptions;
        private String thumbnailPath;
        private List<ProductImageResponseDto.ProductImageInfo> productImagePaths;

        public Product toEntity() {
            return Product.builder()
                    .productId(0)
                    .name(this.getName())
                    .description(this.getDescription())
                    .isDeleted(this.isDeleted())
                    .productState(this.getProductState())
                    .thumbnailPath(this.getThumbnailPath())
                    .build();
        }

        public static ProductGet of(Product product) {
            return ProductGet.builder()
                    .productId(product.getProductId())
                    .name(product.getName())
                    .description(product.getDescription())
                    .isDeleted(product.isDeleted())
                    .productState(product.getProductState())
                    .isSelling(product.isSelling())
                    .createdAt(product.getCreatedAt())
                    .price(product.getPrice())
                    .discountId(Objects.isNull(product.getDiscount()) ? null : product.getDiscount().getDiscountId())
                    .summaryDescription(product.getSummaryDescription())
                    .simpleDescription(product.getSimpleDescription())
                    .discountedPrice(product.getDiscountedPrice())
                    .thumbnailPath(product.getThumbnailPath())
                    .build();
        }
    }

    @Getter
    @Setter
    @Builder
    public static class ProductOptionGet {
        private Long productOptionId;
        private String productOptionName;
        private List<ProductResponseDto.GetProductOptionDetail> productOptionDetails;
        private int stockQuantity;
        private int additionalPrice;

        public static ProductOptionGet of(ProductOption productOption) {
            return ProductOptionGet.builder()
                    .productOptionId(productOption.getProductOptionId())
                    .productOptionName(productOption.getProductOptionName())
                    .stockQuantity(productOption.getStockQuantity())
                    .additionalPrice(productOption.getAdditionalPrice())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class GetProductOptionDetail {
        private String productOptionType;
        private String productOptionDetailName;

        public static ProductResponseDto.GetProductOptionDetail of(ProductOptionDetail productOptionDetail) {
            return GetProductOptionDetail.builder()
                    .productOptionType(productOptionDetail.getProductOptionType())
                    .productOptionDetailName(productOptionDetail.getProductOptionDetailName())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class ProductGetForUsers {
        private long productId;
        private String productName;
        private int price;
        private int discountedPrice;
        @Setter
        private DiscountResponseDto.DiscountInfo discountInfo;
        @Setter
        private List<ProductResponseDto.GetProductTag> tag;
        private String thumbnailPath;

        public static ProductGetForUsers of(Product product) {
            ProductGetForUsers productDto = ProductGetForUsers.builder()
                    .productId(product.getProductId())
                    .productName(product.getName())
                    .price(product.getPrice())
                    .discountedPrice(product.getDiscountedPrice())
                    .thumbnailPath(product.getThumbnailPath())
                    .build();
            if (Objects.nonNull(product.getDiscount())) {
                productDto.setDiscountInfo(DiscountResponseDto.DiscountInfo.of(product.getDiscount()));
            }
            productDto.setTag(ProductResponseDto.GetProductTag.of(product.getProductTags()));
            return productDto;
        }
    }

    @Getter
    @AllArgsConstructor
    public static class GetProductTag {
        private Long productTagId;
        private String productTagName;

        public static List<GetProductTag> of(List<ProductTag> productTags) {
            return productTags.stream()
                    .map(productTag -> new GetProductTag(productTag.getId(), productTag.getTag().getTagName()))
                    .toList();
        }
    }

    @Getter
    @AllArgsConstructor
    public static class GetTag {
        private Long tagId;
        private String tagName;

        public static List<GetTag> of(List<Tag> tags) {
            return tags.stream()
                    .map(tag -> new GetTag(tag.getTagId(), tag.getTagName()))
                    .toList();
        }
    }

    @Getter
    @AllArgsConstructor
    public static class ProductCategoryMappingGet {
        private int productCategoryMappingId;
        private int categoryId;
        private long productId;

        public static ProductResponseDto.ProductCategoryMappingGet of(ProductCategoryMapping productCategoryMapping) {
            return new ProductResponseDto.ProductCategoryMappingGet(
                    productCategoryMapping.getProductCategoryMappingId(),
                    productCategoryMapping.getCategory().getCategoryId(),
                    productCategoryMapping.getProduct().getProductId()
            );
        }
    }
}
