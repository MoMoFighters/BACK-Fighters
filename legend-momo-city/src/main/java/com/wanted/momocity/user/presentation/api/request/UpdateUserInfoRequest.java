package com.wanted.momocity.user.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateUserInfoRequest(
        @Schema(description = "변경할 프로필 이미지 item 이름 ")
        String itemName,

        @Schema(description = "변경할 닉네임")
        @Size(min = 1,  message = "닉네임은 최소 1글자입니다.")
        String nickname,

        @Schema(description = "기존 비밀번호")
        String currentPassword,

        @Schema(description = "변경할 비밀번호")
        @Pattern(
                regexp = "^(?=.*[!@#$%^&*(),.?\":{}|<>]).{8,}$",
                message = "비밀번호는 특수기호 포함 8자리 이상이어야 합니다."
        )
        String password
) {
}
