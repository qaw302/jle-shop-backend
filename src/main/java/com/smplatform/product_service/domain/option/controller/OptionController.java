package com.smplatform.product_service.domain.option.controller;

import com.smplatform.product_service.annotation.AdminOnly;
import com.smplatform.product_service.domain.option.dto.OptionRequestDto;
import com.smplatform.product_service.domain.option.dto.OptionResponseDto;
import com.smplatform.product_service.domain.option.service.OptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/options")
@RequiredArgsConstructor
@Tag(name = "Option", description = "Option management APIs")
public class OptionController {
    private final OptionService optionService;

    @PostMapping
    @Operation(
            summary = "옵션 등록",
            description = "옵션 타입을 등록합니다"
    )
    public ResponseEntity<Long> createOption(@RequestBody OptionRequestDto.SaveOption body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(optionService.saveOption(body));
    }

    @GetMapping
    @Operation(summary = "옵션 목록 조회", description = "옵션 목록을 조회합니다")
    public ResponseEntity<List<OptionResponseDto.GetOption>> getOption(@PageableDefault Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(optionService.getOptions(pageable));
    }

    @AdminOnly
    @PostMapping("/delete-option")
    @Operation(
            summary = "옵션 삭제",
            description = "옵션 타입을 삭제합니다 (관리자)"
    )
    public ResponseEntity<Void> updateProduct(@RequestBody OptionRequestDto.DeleteOption deleteOptionDto) {
        optionService.deleteOptionType(deleteOptionDto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
