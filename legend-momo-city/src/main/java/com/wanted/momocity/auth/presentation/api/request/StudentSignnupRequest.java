package com.wanted.momocity.auth.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import software.amazon.awssdk.annotations.NotNull;

@Schema(description = "학생 수강 신청 요청")
public record StudentSignnupRequest(

    @Schema(description = "회원가입 할 사용자 이메일 - 로그인 시 id로 사용")
    @NotNull String email,

    @Schema(description = "회원가입 할 사용자 비밀번호 - 로그인 시 비밀번호로 사용")
    @NotNull String password,

    @Schema(description = "회원가입 할 사용자 이름")
    @NotNull String name

    ) {


}
