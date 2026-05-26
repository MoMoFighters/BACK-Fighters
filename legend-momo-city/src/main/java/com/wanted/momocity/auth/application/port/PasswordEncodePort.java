package com.wanted.momocity.auth.application.port;

public interface PasswordEncodePort {
    // 비밀번호 엔코딩
    String encode(String rawPassword);

}
