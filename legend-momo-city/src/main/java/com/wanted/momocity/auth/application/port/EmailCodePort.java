package com.wanted.momocity.auth.application.port;

public interface EmailCodePort {
    // 레디스 사용을 위해 값을 저장하는 포트
    void save(String email, String code, long ttlSeconds);

    String find(String email); // 메일로 보낸 인증코드 값 조회
    void delete(String email); // 인증 후 삭제
}
