package com.wanted.momocity.auth.presentation.api.request;

import com.wanted.momocity.auth.domain.model.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import software.amazon.awssdk.annotations.NotNull;

@Schema(description = "강사 수강 신청 요청")
public record TeacherSignupRequest(

        @Schema(description = "회원가입 할 사용자 이메일 - 로그인 시 id로 사용")
        @NotNull String email,

        @Schema(description = "회원가입 할 사용자 비밀번호 - 로그인 시 비밀번호로 사용")
        @NotNull String password,

        @Schema(description = "회원가입 할 사용자 이름")
        @NotNull String name,

         @Schema(description = "회원가입 할 사용자 카테고리 - 강사 본인이 가르칠 카테고리")
        @NotNull Category category,

        @Schema(description = "회원가입 할 사용자 증빙자료 - 강사 회원가입을 위한 증빙 자료")
        @NotNull String proof


) {


}