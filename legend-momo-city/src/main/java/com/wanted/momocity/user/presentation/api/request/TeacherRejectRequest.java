package com.wanted.momocity.user.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record TeacherRejectRequest(

        // 강사 거절하는 요청
        @Schema(description = "반려 사유 (REJECT 시 필수, 최소 10자)", example = "자격증 서류가 불충분합니다.",minLength = 10)
        String reason
) {
}
