package com.wanted.momocity.auth.application.command;

public record OAuthUserInfoCommand(

        //  API 호출 해서 받아온 유저 정보를 담음
        //  카카오/구글 API 호출해서 받아온 값들
        String providerId,  // 고유 ID
        String email,       // 카카오는 null 가능
        String name
) {
}
