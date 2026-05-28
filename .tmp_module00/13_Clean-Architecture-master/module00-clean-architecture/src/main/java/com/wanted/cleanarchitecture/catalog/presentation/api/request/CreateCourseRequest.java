package com.wanted.cleanarchitecture.catalog.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "강의 생성 요청")
public record CreateCourseRequest(
        @Schema(description = "강의를 작성하는 교육자 ID", example = "100")
        @NotNull Long authorId,

        @Schema(description = "강의 제목", example = "Clean Architecture 101")
        @NotBlank String title,

        @Schema(description = "강의 설명", example = "DDD와 Clean Architecture를 함께 배우는 강의")
        String description
) {
}
