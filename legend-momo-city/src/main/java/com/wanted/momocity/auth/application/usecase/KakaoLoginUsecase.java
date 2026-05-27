package com.wanted.momocity.auth.application.usecase;

import com.wanted.momocity.auth.application.command.SocialLoginCommand;
import com.wanted.momocity.auth.presentation.api.response.LoginResponse;

public interface KakaoLoginUsecase {

    LoginResponse socialLogin(SocialLoginCommand command);

}
