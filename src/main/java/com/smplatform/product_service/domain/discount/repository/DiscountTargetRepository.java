package com.smplatform.product_service.domain.discount.repository;

import com.smplatform.product_service.domain.discount.entity.Discount;
import com.smplatform.product_service.domain.discount.entity.DiscountTarget;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface DiscountTargetRepository extends JpaRepository<DiscountTarget, Long> {

    @Query("""
            select dt.discount
            from DiscountTarget dt
            where dt.applyType = :applyType
              and dt.targetId = :targetId
              and :now between dt.discount.discountStartDate and dt.discount.discountEndDate
            order by dt.discount.discountStartDate desc
            """)
    List<Discount> findActiveDiscountsByTarget(@Param("applyType") Discount.ApplyType applyType,
                                               @Param("targetId") Long targetId,
                                               @Param("now") LocalDateTime now,
                                               Pageable pageable);

    @Query("""
            select dt.discount
            from DiscountTarget dt
            where dt.applyType = :applyType
              and dt.targetId is null
              and :now between dt.discount.discountStartDate and dt.discount.discountEndDate
            order by dt.discount.discountStartDate desc
            """)
    List<Discount> findActiveDiscountsByApplyType(@Param("applyType") Discount.ApplyType applyType,
                                                  @Param("now") LocalDateTime now,
                                                  Pageable pageable);

    void deleteByDiscount(Discount discount);
}
