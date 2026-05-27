package com.wanted.momocity.auth.application.service;

import com.wanted.momocity.auth.application.port.LoadUserPort;
import com.wanted.momocity.auth.application.usecase.LoginCompletedUsecase;
import com.wanted.momocity.auth.domain.exception.UserNotFoundException;
import com.wanted.momocity.auth.domain.model.User;
import com.wanted.momocity.auth.presentation.api.response.LoginCompletedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class LoginCompletedService implements LoginCompletedUsecase {

    private final LoadUserPort loadUserPort;


    @Override
    public LoginCompletedResponse getInfo(String userId) {
        User loginUser = loadUserPort.findById(Long.parseLong(userId))
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));
        return new LoginCompletedResponse(loginUser.getRole(),loginUser.getIsTempPwd(),loginUser.getNickname());
    }
}
