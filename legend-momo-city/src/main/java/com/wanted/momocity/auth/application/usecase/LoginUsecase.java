package com.wanted.momocity.auth.application.usecase;

import com.wanted.momocity.auth.application.command.LoginCommand;
import com.wanted.momocity.auth.presentation.api.response.LoginResponse;

public interface LoginUsecase {
    LoginResponse login(LoginCommand command);
}
