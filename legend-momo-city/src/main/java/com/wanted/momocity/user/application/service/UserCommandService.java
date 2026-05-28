package com.wanted.momocity.user.application.service;

import com.wanted.momocity.auth.application.port.PasswordEncodePort;
import com.wanted.momocity.user.application.command.NicknameRegisterCommand;
import com.wanted.momocity.user.application.command.UpdateUserInfoCommand;
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
    private final UserPolicy userPolicy;
    private final PasswordEncodePort passwordEncodePort;

    @Override
    public String registerNickname(NicknameRegisterCommand command) {
        userPolicy.nicknamePolicy(command.nickname());
        userRepository.registerNickname(command.userId(), command.nickname());
        return command.nickname();
    }

    @Override
    public void updateUserInfo(UpdateUserInfoCommand command) {
        // 닉네임 있으면 중복 확인
        if (command.nickname() != null) {
            userPolicy.nicknamePolicy(command.nickname());
        }

        String encodedPassword = null;
        if (command.password() != null) {
            String storedPassword = userRepository.findPasswordById(command.userId());
            userPolicy.passwordPolicy(command.currentPassword(), command.password(), storedPassword);
            encodedPassword = passwordEncodePort.encode(command.password());  // 검증 통과 후 암호화
        }

        userRepository.updateUserInfo(new UpdateUserInfoCommand(
                command.userId(),
                command.profileImageUrl(),
                command.nickname(),
                null,//현재 비번은 저장 안 함
                encodedPassword
        ));
    }
}