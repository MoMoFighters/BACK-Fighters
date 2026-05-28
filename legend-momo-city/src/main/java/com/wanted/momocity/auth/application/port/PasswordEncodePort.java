package com.wanted.momocity.auth.application.port;

public interface PasswordEncodePort {
    // 비밀번호 엔코딩
    String encode(String rawPassword);

    // 비빌번호 변경할 때 - 기존 비번이랑 새로운 비번 일치하면 못 바꿈
    boolean matches(String rawPassword, String encodedPassword);

}
