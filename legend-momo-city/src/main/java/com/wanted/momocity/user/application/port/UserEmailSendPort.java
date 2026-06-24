package com.wanted.momocity.user.application.port;

import com.wanted.momocity.user.domain.model.Status;

public interface UserEmailSendPort {
    void sendTeacherResult(String toEmail, Status status, String reason);

}
