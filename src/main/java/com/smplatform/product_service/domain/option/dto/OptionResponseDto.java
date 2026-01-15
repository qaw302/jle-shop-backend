package com.smplatform.product_service.domain.option.dto;

import com.smplatform.product_service.domain.option.entity.OptionType;
import com.smplatform.product_service.domain.option.entity.OptionValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class OptionResponseDto {

    @AllArgsConstructor
    @Builder
    @Getter
    @ToString
    @Schema(name = "OptionResponse", description = "옵션 타입 응답")
    public static class GetOption {
        @Schema(description = "옵션 타입 ID", example = "1")
        private long optionTypeId;
        @Schema(description = "옵션 타입명", example = "COLOR")
        private String optionTypeName;
        @Setter
        @Schema(description = "옵션 값 목록")
        private List<OptionResponseDto.GetOptionValue> optionValues;
        @Schema(description = "등록 시각", example = "2024-01-01T12:00:00")
        private LocalDateTime createdAt;

        public static OptionResponseDto.GetOption of(OptionType optionType) {
            return GetOption.builder()
                    .optionTypeId(optionType.getOptionTypeId())
                    .optionTypeName(optionType.getOptionTypeName())
                    .createdAt(optionType.getCreatedAt())
                    .build();
        }
    }

    @AllArgsConstructor
    @Builder
    @Getter
    @Schema(name = "OptionValueResponse", description = "옵션 값 응답")
    public static class GetOptionValue {
        @Schema(description = "옵션 값 ID", example = "10")
        private long optionValueId;
        @Schema(description = "옵션 값명", example = "RED")
        private String optionValueName;

        public static OptionResponseDto.GetOptionValue of(OptionValue optionValue) {
            return GetOptionValue.builder()
                    .optionValueId(optionValue.getOptionValueId())
                    .optionValueName(optionValue.getOptionValueName())
                    .build();
        }
    }
}
