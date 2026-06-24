package com.wanted.momocity.user.presentation.api.request;

import com.wanted.momocity.global.domain.model.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Schema(description = "강사 수강 신청 요청")
public record TeacherApplyRequest(

        @Schema(description = "기존에 사용하던 닉네임" +
                "용도 1. 강사가 기존에 본인이 학생이었을 때 사용하던 닉네임을 그대로 사용할 수 있게" +
                "    2. 반려된 강사가 다시 강사 재신청 할 때 이전에 본인이 썼던 닉네임 그대로 사용할 수 있게 ")
        @NotBlank(message = "기존 닉네임은 필수입니다.")
        String currentNickname,

        @Schema(description = "강사가 사이트 내에서 사용할 닉네임")
        @NotBlank(message = "강사 활동명을 입력해주세요.")
        @Size(min = 1,  message = "강사 활동명은 최소 1글자입니다.")
        String nickname,

        @Schema(description = "회원가입 할 사용자 카테고리 - 강사 본인이 가르칠 카테고리")
        @NotNull(message = "카테고리를 선택해주세요.")
        Category category,

        @Schema(
                description = "회원가입 할 사용자 증빙자료 - 강사 회원가입을 위한 증빙 자료",
                type = "string",
                format = "binary")
        @NotNull(message = "증빙 자료는 필수 제출입니다.")
        MultipartFile proof


) {


}