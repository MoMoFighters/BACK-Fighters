package com.wanted.momocity.auth.application.usecase;

import com.wanted.momocity.auth.application.command.EmailVerifyCommand;
import com.wanted.momocity.auth.presentation.api.response.LoginCompletedResponse;

public interface AuthQueryUsecase {

    // 이메일 인증
    void emailVerify(EmailVerifyCommand command);

    // 로그인 성공 시 정보 전달
    LoginCompletedResponse getInfo(String email);


}
