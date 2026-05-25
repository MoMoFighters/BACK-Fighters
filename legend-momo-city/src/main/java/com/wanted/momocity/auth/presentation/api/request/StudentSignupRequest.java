package com.wanted.momocity.auth.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "학생 수강 신청 요청")
public record StudentSignupRequest(

    @Schema(description = "회원가입 할 사용자 이메일 - 로그인 시 id로 사용")
    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    String email,

    @Schema(description = "회원가입 할 사용자 비밀번호 - 로그인 시 비밀번호로 사용")
    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Pattern(
            regexp = "^(?=.*[!@#$%^&*(),.?\":{}|<>]).{8,}$",
            message = "비밀번호는 특수기호 포함 8자리 이상이어야 합니다."
    )
    String password,

    @Schema(description = "회원가입 할 사용자 이름")
    @NotBlank(message = "이름을 입력해주세요.")
    String name

    ) {


}
