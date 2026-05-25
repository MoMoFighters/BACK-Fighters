package com.wanted.momocity.auth.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import software.amazon.awssdk.annotations.NotNull;

@Schema(description = "자체 로그인 요청")
public record LoginRequest(
        @Schema(description = "로그인 할 사용자 이메일 - 이메일을 id로 하여 로그인 진행 ")
        @NotNull String email,

        @Schema(description = "로그인 할 사용자 비밀번호")
        @NotNull String password
) {

}
