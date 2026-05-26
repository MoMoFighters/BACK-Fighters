package com.wanted.momocity.auth.application.command;

public record SocialLoginCommand(
        // 프론트가 백엔드에 보내주는 인가 코드
        String code
) {
}
