package com.wanted.momocity.auth.application.usecase;

import com.wanted.momocity.auth.application.command.LoginCommand;
import com.wanted.momocity.auth.application.result.LoginResult;

public interface LoginUsecase {
    LoginResult login(LoginCommand command);
}
