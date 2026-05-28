package com.wanted.momocity.user.application.usecase;

import com.wanted.momocity.user.application.command.NicknameRegisterCommand;

public interface UserCommandUsecase {

    String registerNickname(NicknameRegisterCommand command);

}
