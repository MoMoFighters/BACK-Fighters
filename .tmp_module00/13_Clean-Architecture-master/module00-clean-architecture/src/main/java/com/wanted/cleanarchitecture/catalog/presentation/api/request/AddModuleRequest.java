package com.wanted.cleanarchitecture.catalog.presentation.api.request;

import com.wanted.cleanarchitecture.catalog.domain.model.ContentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "강의 모듈 추가 요청")
public record AddModuleRequest(
        @Schema(description = "모듈 제목", example = "Why Boundaries Matter")
        @NotBlank String title,

        @Schema(description = "콘텐츠 타입", example = "TEXT")
        @NotNull ContentType contentType,

        @Schema(description = "섹션 안에서의 모듈 순서", example = "1")
        @Min(1) int moduleOrder
) {
}
