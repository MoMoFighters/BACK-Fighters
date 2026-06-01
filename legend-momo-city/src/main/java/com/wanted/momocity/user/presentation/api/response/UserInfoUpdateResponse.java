package com.wanted.momocity.user.presentation.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserInfoUpdateResponse(

        @Schema(description = "비밀번호 변경 여부", example = "true")
        Boolean isPwdChanged
) {
}
