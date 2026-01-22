package com.smplatform.product_service.domain.product.service.impl;

import com.smplatform.product_service.domain.discount.entity.Discount;
import com.smplatform.product_service.domain.discount.repository.DiscountRepository;
import com.smplatform.product_service.domain.discount.repository.DiscountTargetRepository;
import com.smplatform.product_service.domain.product.dto.ProductImageResponseDto;
import com.smplatform.product_service.domain.product.dto.ProductRequestDto;
import com.smplatform.product_service.domain.product.dto.ProductResponseDto;
import com.smplatform.product_service.domain.product.entity.*;
import com.smplatform.product_service.domain.product.exception.ProductNotFoundException;
import com.smplatform.product_service.domain.product.repository.*;
import com.smplatform.product_service.domain.product.service.ProductCategoryMappingService;
import com.smplatform.product_service.domain.product.service.ProductImageService;
import com.smplatform.product_service.domain.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final DiscountRepository discountRepository;
    private final DiscountTargetRepository discountTargetRepository;
    private final ProductOptionRepository productOptionRepository;
    private final ProductOptionDetailRepository productOptionDetailRepository;
    private final ProductImageService productImageService;
    private final TagRepository tagRepository;
    private final ProductTagRepository productTagRepository;
    private final ProductCategoryMappingRepository productCategoryMappingRepository;

    private final ProductCategoryMappingService productCategoryMappingService;

    /**
     * 단일 제품 조회
     *
     * @param productId
     * @return
     */
    @Override
    @Transactional(readOnly = true)
    public ProductResponseDto.ProductGet getProduct(long productId) {
        Product product = productRepository.findByProductIdAndIsDeletedFalse(productId)
                .orElseThrow(() -> new ProductNotFoundException(String.format("product { %d } not found", productId)));

        List<ProductOption> productOptions = productOptionRepository.findAllByProductProductId(productId);

        List<ProductResponseDto.ProductOptionGet> productOptionDtos = productOptions.stream()
                .map(productOption -> {
                    List<ProductOptionDetail> productOptionDetails = productOptionDetailRepository
                            .findAllByProductOption_ProductOptionId(productOption.getProductOptionId());
                    List<ProductResponseDto.GetProductOptionDetail> productOptionDetailDtos = productOptionDetails.stream()
                            .map(ProductResponseDto.GetProductOptionDetail::of)
                            .toList();
                    ProductResponseDto.ProductOptionGet productOptionDto = ProductResponseDto.ProductOptionGet.of(productOption);
                    productOptionDto.setProductOptionDetails(productOptionDetailDtos);
                    return productOptionDto;
                }).toList();

        ProductResponseDto.ProductGet productDto = ProductResponseDto.ProductGet.of(product);
        productDto.setProductOptions(productOptionDtos);
        productDto.setProductImagePaths(productImageService.getProductProductImageList(productId));
        return productDto;
    }

    /**
     * 단일 제품 등록
     *
     * @param productDto
     * @return
     */
    @Override
    @Transactional
    public String saveProduct(ProductRequestDto.ProductSave productDto) {
        // 상품 저장
        Product productDtoEntity = productDto.toEntity();
        Discount discount = resolveDiscountForNewProduct(productDto.getDiscountId(), productDto.getCategoryId());
        productDtoEntity.setDiscount(discount);

        Product product = productRepository.save(productDtoEntity);

        productCategoryMappingService.save(productDto.getCategoryId(), product.getProductId());

        // 상품옵션 저장
        List<ProductRequestDto.ProductOptionSave> productOptionDtos = productDto.getProductOptions();
        List<ProductOption> productOptions = productOptionDtos.stream()
                .map(productOptionDto -> {
                    ProductOption productOption = productOptionDto.toEntity();
                    productOption.setProduct(product);
                    return productOption;
                }).toList();
        List<ProductOption> savedProductOptions = productOptionRepository.saveAll(productOptions);

        // 옵션구성 저장
        List<ProductOptionDetail> productOptionDetails = IntStream.range(0, savedProductOptions.size()).boxed()
                .flatMap(index -> {
                    ProductOption savedProductOption = savedProductOptions.get(index);
                    return productOptionDtos.get(index).getProductOptionDetails().stream()
                            .map(dto -> {
                                ProductOptionDetail productOptionDetail = dto.toEntity();
                                productOptionDetail.setProductOption(savedProductOption);
                                return productOptionDetail;
                            });
                }).toList();
        productOptionDetailRepository.saveAll(productOptionDetails);

        // 상품이미지 저장
        productImageService.saveProductImages(
                product.getProductId(),
                productDto.getProductImages().getPaths()
        );

        // 상품 태그 저장
        for (ProductRequestDto.TagSave tagDto : productDto.getTags()) {
            Optional<Tag> optionalTag = tagRepository.findByTagName(tagDto.getTagName());

            if (optionalTag.isPresent()) {
                productTagRepository.save(new ProductTag(0L, product, optionalTag.get()));
            } else {
                Tag tag = tagRepository.save(tagDto.toEntity());
                productTagRepository.save(new ProductTag(0L, product, tag));
            }
        }

        return String.valueOf(product.getProductId());
    }

    private Discount resolveDiscountForNewProduct(int discountId, int categoryId) {
        if (discountId > 0) {
            return discountRepository.findById(discountId).orElse(null);
        }
        LocalDateTime now = LocalDateTime.now();
        List<Discount> categoryDiscounts = discountTargetRepository.findActiveDiscountsByTarget(
                Discount.ApplyType.SPECIFIC_CATEGORY,
                (long) categoryId,
                now,
                PageRequest.of(0, 1)
        );
        if (!categoryDiscounts.isEmpty()) {
            return categoryDiscounts.get(0);
        }
        List<Discount> allDiscounts = discountTargetRepository.findActiveDiscountsByApplyType(
                Discount.ApplyType.ALL,
                now,
                PageRequest.of(0, 1)
        );
        if (!allDiscounts.isEmpty()) {
            return allDiscounts.get(0);
        }
        return null;
    }


    /**
     * 단일 제품 수정
     *
     * @param productDto
     * @return
     */
    @Override
    public String updateProduct(ProductRequestDto.ProductUpdate productDto) {
        Product product = productRepository.findById(productDto.getId())
                .orElseThrow(() -> new ProductNotFoundException(String.format("product id : %d not found", productDto.getId())));

        // TODO: 카테고리 수정

        // 할인 수정
        if (!Objects.isNull(productDto.getDiscountId())) {
            product.setDiscount(discountRepository.findById(productDto.getDiscountId())
                    .orElseThrow(() -> new RuntimeException(String.format("discount id : %d not found", productDto.getDiscountId())))
            );
        }
        BeanUtils.copyProperties(productDto, product, "id");

        // 썸네일 수정
        updateProductImage(
                product,
                productDto.getProductImagePaths().getPaths()
        );

        return String.valueOf(productRepository.save(product).getProductId());
    }

    @Override
    @Transactional
    public String deleteProduct(long productId) {
        Product product = productRepository.findByProductIdAndIsDeletedFalse(productId)
                .orElseThrow(() -> new ProductNotFoundException(String.format("product { %d } not found", productId)));

        product.setDeleted(true);
        product.setSelling(false);

        return String.valueOf(productRepository.save(product).getProductId());
    }

    private void updateProductImage(Product product, List<String> paths) {
        List<ProductImageResponseDto.ProductImageInfo> originalProductImageInfoList =
                productImageService.getProductProductImageList(product.getProductId());

        // 삭제된 항목 제거
        List<Long> toDeleteIds = originalProductImageInfoList.stream()
                .filter(original -> !paths.contains(original.getPath()))
                .map(ProductImageResponseDto.ProductImageInfo::getProductImageId)
                .toList();

        if (!toDeleteIds.isEmpty()) {
            productImageService.deleteProductImage(toDeleteIds);
        }

        // 추가된 항목 저장
        List<String> originalPaths = originalProductImageInfoList.stream()
                .map(ProductImageResponseDto.ProductImageInfo::getPath)
                .toList();

        List<String> toAdd = paths.stream()
                .filter(path -> !originalPaths.contains(path))
                .toList();

        if (!toAdd.isEmpty()) {
            productImageService.saveProductImages(product.getProductId(), toAdd);
        }
    }

    /**
     * 관리자용 제품 조회
     *
     * @param pageable
     * @return
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDto.ProductGet> getProducts(ProductRequestDto.AdminProductSearchCondition condition,
                                                           Pageable pageable) {
        if (Objects.isNull(condition) || condition.isConditionEmpty()) {
            return productRepository.findAll(pageable).stream()
                    .map(ProductResponseDto.ProductGet::of)
                    .toList();
        } else {
            List<ProductResponseDto.ProductGet> resultProducts = new ArrayList<>();
            Page<Product> products = productRepository.searchProducts(condition, pageable);

            for (Product product : products) {
                ProductResponseDto.ProductGet productDto = ProductResponseDto.ProductGet.of(product);
                productDto.setProductImagePaths(productImageService.getProductProductImageList(product.getProductId()));
                resultProducts.add(productDto);
            }
            return resultProducts;
        }
    }


    /**
     * 일반 사용자 용 전체 제품 조회
     *
     * @param categoryId
     * @param pageable
     * @return
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDto.ProductGetForUsers> getProductsForUsers(int categoryId,
                                                                           ProductRequestDto.UserProductSearchCondition condition,
                                                                           Pageable pageable) {

        if (Objects.isNull(condition) || condition.isConditionEmpty()) {
            if (categoryId == 0) {
                return productRepository.findAllByIsDeletedFalse(pageable)
                        .map(ProductResponseDto.ProductGetForUsers::of)
                        .toList();
            } else {
                return productCategoryMappingRepository.findAllByCategoryId(categoryId).stream()
                        .map(ProductResponseDto.ProductGetForUsers::of)
                        .toList();
            }
        } else {
            return productRepository.searchUserProducts(categoryId, condition, pageable).stream()
                    .map(ProductResponseDto.ProductGetForUsers::of)
                    .toList();
        }
    }

    @Override
    public List<ProductResponseDto.GetTag> getProductsTags() {
        return ProductResponseDto.GetTag.of(tagRepository.findAll());
    }

    @Override
    public List<ProductResponseDto.ProductCategoryMappingGet> getProductCategoryMappings() {
        return productCategoryMappingRepository.findAll().stream()
                .map(ProductResponseDto.ProductCategoryMappingGet::of)
                .toList();
    }
}
