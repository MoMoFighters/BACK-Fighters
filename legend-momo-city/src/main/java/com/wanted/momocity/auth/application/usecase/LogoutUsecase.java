package com.wanted.momocity.auth.application.usecase;

import com.wanted.momocity.auth.application.command.LogoutCommand;

public interface LogoutUsecase {
    void logout(LogoutCommand command);

}
