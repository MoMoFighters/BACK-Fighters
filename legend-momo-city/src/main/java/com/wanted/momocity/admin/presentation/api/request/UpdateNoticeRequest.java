package com.wanted.momocity.admin.presentation.api.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateNoticeRequest(

        // 필수 값 검증 - null 또는 공백이면 400 반환
        @NotBlank String title,
        @NotBlank String content
) {
}
