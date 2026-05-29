package com.wanted.momocity.user.application.port;

public interface UserEmailSendPort {
    void sendTeacherResult(String toEmail, String status, String reason);

}
