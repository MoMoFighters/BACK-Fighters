package com.wanted.momocity.auth.application.usecase;

import com.wanted.momocity.auth.application.command.EmailVerifyCommand;

public interface EmailVerifyUsecase {
    void emailVerify(EmailVerifyCommand command);
}
