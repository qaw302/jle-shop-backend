package com.smplatform.product_service.domain.discount.controller;

import com.smplatform.product_service.domain.discount.dto.DiscountRequestDto;
import com.smplatform.product_service.domain.discount.dto.DiscountResponseDto;
import com.smplatform.product_service.domain.discount.service.DiscountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@CrossOrigin("*")
@RequestMapping("/v1/discounts")
@Tag(name = "Discount", description = "Discount management APIs")
@RequiredArgsConstructor
@Slf4j
public class DiscountController {
    private final DiscountService discountService;

    @GetMapping
    @Operation(summary = "할인코드 조회", description = "해당 조건의 할인코드 조회")
    public ResponseEntity<List<DiscountResponseDto.DiscountInfo>> getDiscountList(@RequestParam(name = "referenceDate", required = false) String referenceDate,
                                                                                  @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
                                                                                  @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
                                                                                  @RequestParam(name = "discountName", required = false) String discountName) {
        List<DiscountResponseDto.DiscountInfo> discountInfo = discountService.getDiscountList(referenceDate, startDate, endDate, discountName);

        return ResponseEntity.status(HttpStatus.OK).body(discountInfo);
    }

    @PostMapping
    @Operation(summary = "할인코드 등록", description = "해당 옵션의 할인코드 등록")
    public ResponseEntity<String> createDiscount(@RequestBody DiscountRequestDto.DiscountRegister discountRequestDto) {

        String id = discountService.createDiscount(discountRequestDto);

        return ResponseEntity.status(HttpStatus.OK).body(id);
    }

    @PostMapping("/delete-discount")
    @Operation(summary = "할인코드 삭제", description = "전달된 할인코드 삭제")
    public ResponseEntity<String> deleteDiscount(@RequestBody DiscountRequestDto.DeleteDiscount discountRequestDto) {

        String id = discountService.deleteDiscount(discountRequestDto);

        return ResponseEntity.status(HttpStatus.OK).body(id);
    }
}
