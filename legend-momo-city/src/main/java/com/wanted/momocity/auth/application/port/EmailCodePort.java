package com.wanted.momocity.auth.application.port;

public interface EmailCodePort {
    // 레디스 사용을 위해 값을 저장하는 포트
    void save(String email, String code, long ttlSeconds);

    String find(String email); // 메일로 보낸 인증코드 값 조회
    void delete(String email); // 인증 후 인증코드 삭제 삭제

    void saveVerified(String email, long ttlSeconds);  // 인증 완료 표시 저장
    boolean isVerified(String email); // 인증 여부 확인
    void deleteVerified(String email);   // 가입 완료 후에 인증완료 표시 삭제

    void saveTempPassword(String email, long ttlSeconds);
    boolean isTempPasswordVerified(String email);
    void deleteTempPassword(String email);

}
