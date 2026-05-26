package com.wanted.momocity.auth.application.usecase;

import com.wanted.momocity.auth.presentation.api.response.LoginCompletedResponse;

public interface LoginCompletedUsecase {
    LoginCompletedResponse getInfo(String email);
}
