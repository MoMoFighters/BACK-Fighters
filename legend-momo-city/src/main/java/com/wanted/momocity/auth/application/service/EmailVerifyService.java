package com.wanted.momocity.auth.application.service;

import com.wanted.momocity.auth.application.command.EmailVerifyCommand;
import com.wanted.momocity.auth.application.port.EmailCodePort;
import com.wanted.momocity.auth.application.usecase.EmailVerifyUsecase;
import com.wanted.momocity.auth.domain.exception.InvalidVerificationCodeException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class EmailVerifyService implements EmailVerifyUsecase {

    private final EmailCodePort emailCodePort;

    @Override
    public void emailVerify(EmailVerifyCommand command) {
        String savedCode = emailCodePort.find(command.email());


        if (savedCode == null) { // 만료되었거나 존재하지 않는 경우
            throw new InvalidVerificationCodeException("인증 코드가 만료되었습니다. 재발송 버튼을 눌러 인증코드를 다시 발급받아 입력해주세요.");
        }

        if (!savedCode.equals(command.code())) { // 코드가 틀린 경우
            throw new InvalidVerificationCodeException("인증 코드가 올바르지 않습니다.");
        }

        emailCodePort.delete(command.email());
    }
}