package com.wanted.cleanarchitecture.catalog.presentation.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "강의 생성 응답")
public record CreateCourseResponse(
        @Schema(description = "생성된 강의 ID", example = "1")
        Long courseId
) {
}
