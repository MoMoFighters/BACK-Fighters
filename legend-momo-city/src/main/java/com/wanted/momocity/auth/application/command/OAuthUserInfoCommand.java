package com.wanted.momocity.auth.application.command;

public record OAuthUserInfoCommand(

        // 카카오 API 호출 해서 받아온 유저 정보를 담음
        String providerId,  // 카카오 고유 ID
        String email,       // 카카오는 null 가능
        String name
) {
}
