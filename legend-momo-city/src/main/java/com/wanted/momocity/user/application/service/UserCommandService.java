package com.wanted.momocity.user.application.service;

import com.wanted.momocity.user.application.command.NicknameRegisterCommand;
import com.wanted.momocity.user.application.policy.UserPolicy;
import com.wanted.momocity.user.application.usecase.UserCommandUsecase;
import com.wanted.momocity.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserCommandService implements UserCommandUsecase {

    private final UserRepository userRepository;
    private final UserPolicy nicknamePolicy;

    @Override
    public String registerNickname(NicknameRegisterCommand command) {
        nicknamePolicy.nicknamePolicy(command.nickname());
        userRepository.registerNickname(command.userId(), command.nickname());
        return command.nickname();
    }

}
