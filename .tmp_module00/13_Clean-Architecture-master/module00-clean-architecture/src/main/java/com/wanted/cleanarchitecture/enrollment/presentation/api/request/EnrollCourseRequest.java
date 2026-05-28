package com.wanted.cleanarchitecture.enrollment.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "수강 신청 요청")
public record EnrollCourseRequest(
        @Schema(description = "수강 신청하는 사용자 ID", example = "200")
        @NotNull Long userId,

        @Schema(description = "수강 신청할 강의 ID", example = "1")
        @NotNull Long courseId
) {
}
