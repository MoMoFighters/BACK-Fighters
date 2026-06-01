package com.wanted.momocity.auth.application.command;

public record SocialLoginCommand(

        String provider, // api 호출에 따른 전달
        // 프론트가 백엔드에 보내주는 인가 코드
        String code
) {
}
