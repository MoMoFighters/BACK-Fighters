package com.wanted.momocity.user.presentation.api.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record TeacherApproveRequest(
        // 강사 승인하는 요청

        @NotEmpty
        List<Long> userId

) {
}
