package com.wanted.momocity.auth.application.port;

public interface EmailSendPort {
    // 실제 이메일 발송을 위한 포트
    void send(String toEmail, String code);
    void sendTempPassword(String toEmail, String tempPassword);
}
