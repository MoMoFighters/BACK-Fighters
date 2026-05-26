package com.wanted.momocity.teacher.presentation.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/*
 * MS-5: 강사 승인/반려 요청 본문.
 *
 * action: "APPROVE" 또는 "REJECT" (필수)
 * reason: REJECT 시 필수(최소 10자). 검증은 Application Service 단계에서 수행.
 */
public record TeacherActionRequest(

        @NotBlank(message = "action 은 필수입니다")
        @Pattern(regexp = "APPROVE|REJECT", message = "action 값은 APPROVE 또는 REJECT 여야 합니다")
        String action,

        String reason
) {
}
