package com.smplatform.product_service.domain.option.dto;

import com.smplatform.product_service.domain.option.entity.OptionType;
import com.smplatform.product_service.domain.option.entity.OptionValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

public class OptionRequestDto {
    private OptionRequestDto() {
    }

    @Getter
    @Schema(name = "OptionSaveRequest", description = "옵션 타입 등록 요청")
    public static class SaveOption {
        @Schema(description = "옵션 타입명", example = "COLOR")
        private String optionTypeName;
        @Schema(description = "옵션 값 목록")
        private List<OptionRequestDto.SaveOptionValue> optionValues;
        @Schema(description = "등록 시각", example = "2024-01-01T12:00:00")
        private LocalDateTime createdAt;

        public OptionType toEntity() {
            return OptionType.builder()
                    .optionTypeName(optionTypeName)
                    .createdAt(createdAt)
                    .build();
        }
    }

    @Getter
    @Schema(name = "OptionValueSaveRequest", description = "옵션 값 등록 요청")
    public static class SaveOptionValue {
        @Schema(description = "옵션 값명", example = "RED")
        private String optionValueName;

        public OptionValue toEntity() {
            return OptionValue.builder()
                    .optionValueName(optionValueName)
                    .build();
        }
    }

    @Getter
    @Schema(name = "OptionDeleteRequest", description = "옵션 타입 삭제 요청")
    public static class DeleteOption {
        @Schema(description = "옵션 타입 ID", example = "1")
        private long optionTypeId;
    }

}
