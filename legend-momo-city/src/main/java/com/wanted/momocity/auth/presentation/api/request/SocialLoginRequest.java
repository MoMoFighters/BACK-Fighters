package com.wanted.momocity.auth.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record SocialLoginRequest(

        @Schema(description = "프론트 측에서 넘겨주는 소셜 로그인 인가코드")
        @NotBlank(message = "인가 코드는 필수입니다.")
        String code
) {
}
