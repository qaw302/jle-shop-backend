package com.smplatform.product_service.domain.discount.service.impl;

import com.smplatform.product_service.domain.discount.dto.DiscountRequestDto;
import com.smplatform.product_service.domain.discount.dto.DiscountResponseDto;
import com.smplatform.product_service.domain.discount.entity.Discount;
import com.smplatform.product_service.domain.discount.entity.DiscountTarget;
import com.smplatform.product_service.domain.discount.exception.DiscountNotFoundException;
import com.smplatform.product_service.domain.discount.repository.DiscountRepository;
import com.smplatform.product_service.domain.discount.repository.DiscountTargetRepository;
import com.smplatform.product_service.domain.discount.service.DiscountService;
import com.smplatform.product_service.domain.product.entity.Product;
import com.smplatform.product_service.domain.product.exception.ProductNotFoundException;
import com.smplatform.product_service.domain.product.repository.ProductRepository;
import com.smplatform.product_service.domain.product.service.ProductCategoryMappingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class DiscountServiceImpl implements DiscountService {

    private final DiscountRepository discountRepository;
    private final DiscountTargetRepository discountTargetRepository;
    private final ProductCategoryMappingService productCategoryMappingService;
    private final ProductRepository productRepository;

    // 할인코드 목록 조회
    @Override
    public List<DiscountResponseDto.DiscountInfo> getDiscountList(String referenceDate, LocalDateTime startDate, LocalDateTime endDate, String discountName) {
        List<Discount> discountEntityList = discountRepository.findDiscounts(referenceDate, startDate, endDate, discountName);

        return discountEntityList.stream()
                .map(DiscountResponseDto.DiscountInfo::of)
                .toList();
    }

    // 할인코드 등록
    @Override
    @Transactional
    public String createDiscount(DiscountRequestDto.DiscountRegister discountRequestDto) {
        Discount entity = discountRequestDto.toEntity();
        discountRepository.save(entity);
        Discount.ApplyType applyType = Discount.ApplyType.fromTypeNum(discountRequestDto.getApplyType());
        discountTargetRepository.saveAll(buildDiscountTargets(applyType, entity, discountRequestDto.getIds()));
        setProductsById(applyType, entity, discountRequestDto.getIds());
        return String.valueOf(entity.getDiscountId());
    }

    private List<DiscountTarget> buildDiscountTargets(Discount.ApplyType applyType, Discount discount, List<Long> ids) {
        if (applyType == Discount.ApplyType.ALL) {
            return List.of(DiscountTarget.builder()
                    .discount(discount)
                    .applyType(applyType)
                    .targetId(null)
                    .build());
        }
        if (Objects.isNull(ids) || ids.isEmpty()) {
            throw new IllegalArgumentException("할인 적용 대상이 올바르지 않습니다");
        }
        return ids.stream()
                .map(id -> DiscountTarget.builder()
                        .discount(discount)
                        .applyType(applyType)
                        .targetId(id)
                        .build())
                .toList();
    }

    private void setProductsById(Discount.ApplyType applyType, Discount discount, List<Long> ids) {
        List<Product> products;
        if (applyType.getTypeNum() == 0) {
            //전체 제품 할인 적용
            products = productRepository.findAll(Pageable.unpaged())
                    .map(product -> {
                        product.setDiscount(discount);
                        return product;
                    })
                    .toList();
        } else if (applyType.getTypeNum() == 1 && Objects.nonNull(ids)) {
            // 제품 별 할인 적용
            products = ids.stream()
                    .map(productId -> {
                        Product product = productRepository.findById(productId)
                                .orElseThrow(() -> new ProductNotFoundException("product Id : " + productId + " not found"));
                        product.setDiscount(discount);
                        return product;
                    }).toList();
        } else if (applyType.getTypeNum() == 2 && Objects.nonNull(ids)) {
            // 카테고리 별 할인 적용
            products = ids.stream()
                    .flatMap(categoryId ->
                            productCategoryMappingService.getProductsByCategoryId(categoryId.intValue()).stream()
                    ).map(productGet -> {
                        Product product = productRepository.findById(productGet.getProductId())
                                .orElseThrow(() -> new ProductNotFoundException("product Id : " + productGet.getProductId() + " not found"));
                        product.setDiscount(discount);
                        return product;
                    })
                    .toList();
        } else {
            throw new IllegalArgumentException("할인 적용 대상이 올바르지 않습니다");
        }
        productRepository.saveAll(products);
    }

    // 할인코드 삭제
    @Override
    @Transactional
    public String deleteDiscount(DiscountRequestDto.DeleteDiscount discountRequestDto) {

        int discountId = discountRequestDto.getDiscountId();

        // 할인 존재 여부 확인
        Discount discount = discountRepository.findById(discountId)
            .orElseThrow(() -> new DiscountNotFoundException("삭제할 할인 코드가 존재하지 않습니다. ID: " + discountId));

        // 해당 할인이 적용된 모든 상품 조회
        List<Product> productsWithDiscount = productRepository.findAllByDiscount(discount);

        // 상품에서 할인 제거
        if (!productsWithDiscount.isEmpty()) {
            log.info("할인 ID {} 삭제: {} 개의 상품에서 할인 제거", discountId, productsWithDiscount.size());
            productsWithDiscount.forEach(Product::removeDiscount);
            productRepository.saveAll(productsWithDiscount);
        }

        // 할인 삭제
        discountTargetRepository.deleteByDiscount(discount);
        discountRepository.deleteById(discountId);
        log.info("할인 ID {} 삭제 완료", discountId);

        return String.valueOf(discountId);
    }
}
