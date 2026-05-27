package com.wanted.momocity.auth.application.port;

import java.util.concurrent.TimeUnit;

public interface BlacklistPort {

    // 로그아웃할 때 AccessToken을 블랙리스트에 등록
    void addBlacklist(String accessToken, long ttlMillis);

    // 요청이 들어올 때 이 AccessToken이 블랙리스트에 있는지 확인
    boolean isBlacklisted(String accessToken);

}
