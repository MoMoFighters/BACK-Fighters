package com.wanted.cleanarchitecture.enrollment.presentation.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "수강 신청 응답")
public record EnrollCourseResponse(
        @Schema(description = "생성된 수강 신청 ID", example = "1")
        Long enrollmentId
) {
}
