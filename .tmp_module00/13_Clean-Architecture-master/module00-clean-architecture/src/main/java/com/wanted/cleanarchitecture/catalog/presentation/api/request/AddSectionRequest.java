package com.wanted.cleanarchitecture.catalog.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "강의 섹션 추가 요청")
public record AddSectionRequest(
        @Schema(description = "섹션 제목", example = "Introduction")
        @NotBlank String title,

        @Schema(description = "강의 안에서의 섹션 순서", example = "1")
        @Min(1) int sectionOrder
) {
}
