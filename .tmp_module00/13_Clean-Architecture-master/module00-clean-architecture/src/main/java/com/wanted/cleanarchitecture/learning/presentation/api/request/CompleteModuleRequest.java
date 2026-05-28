package com.wanted.cleanarchitecture.learning.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "모듈 완료 처리 요청")
public record CompleteModuleRequest(
        @Schema(description = "학습자 사용자 ID", example = "200")
        @NotNull Long userId,

        @Schema(description = "모듈이 속한 강의 ID", example = "1")
        @NotNull Long courseId
) {
}
