package com.wanted.momocity.auth.application.usecase;

import com.wanted.momocity.auth.application.command.EmailVerifyCommand;

public interface AuthQueryUsecase {

    // 이메일 인증
    void emailVerify(EmailVerifyCommand command);



}
