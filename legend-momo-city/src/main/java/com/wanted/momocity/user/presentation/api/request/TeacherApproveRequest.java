package com.wanted.momocity.user.presentation.api.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record TeacherApproveRequest(
        // 강사 승인하는 요청

        @NotEmpty
        List<@NotNull Long> userId

) {
}
