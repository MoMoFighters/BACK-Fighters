package com.wanted.momocity.user.application.usecase;

import com.wanted.momocity.user.application.command.NicknameRegisterCommand;
import com.wanted.momocity.user.application.command.UpdateUserInfoCommand;

public interface UserCommandUsecase {

    String registerNickname(NicknameRegisterCommand command);

    void updateUserInfo(UpdateUserInfoCommand command);  // 추가


}
