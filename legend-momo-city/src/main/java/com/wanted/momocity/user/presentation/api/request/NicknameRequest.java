package com.wanted.momocity.user.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NicknameRequest(

        @Schema(description = "사용자가 사이트 내에서 사용할 닉네임")
        @NotBlank(message = "닉네임을 입력해주세요.")
        @Size(min = 1,  message = "닉네임은 최소 1글자입니다.")
        String nickname
) {
}
