package com.wanted.momocity.auth.application.port;

import com.wanted.momocity.auth.application.command.OAuthUserInfoCommand;

public interface OAuthClientPort {

    OAuthUserInfoCommand getUserInfo(String code);
    // 외부 소셜 api를 두번 호출해서
    // 첫 번째 호출에서는 토큰 만들고
    // 두 번째 호출에서는 토큰으로 유저 정보를 가져와서 변수에 담음

}
