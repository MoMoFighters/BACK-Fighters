package com.wanted.momocity.auth.application.service;

import com.wanted.momocity.auth.application.command.EmailSendCommand;
import com.wanted.momocity.auth.application.port.*;
import com.wanted.momocity.auth.application.usecase.TempPasswordUsecase;
import com.wanted.momocity.auth.domain.exception.DuplicateEmailException;
import com.wanted.momocity.auth.domain.exception.EmailNotVerifiedException;
import com.wanted.momocity.auth.domain.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@Service
@Transactional
@RequiredArgsConstructor
public class TempPasswordService implements TempPasswordUsecase {

        private final LoadUserPort loadUserPort;
        private final EmailSendPort emailSendPort;
        private final UpdatePasswordPort updatePasswordPort;
        private final PasswordEncodePort passwordEncodePort;
        private final EmailCodePort emailCodePort;

        private static final long EXPIRES_IN_SECONDS = 180L; // 임시 비번 만료시간 3분

    @Override
    public void sendTempPassword(EmailSendCommand command) {

        if (!loadUserPort.findByEmail(command.email()).isPresent()) {
            throw new UserNotFoundException("가입된 이메일이 아닙니다.");
        }

        String tempPassword = generateTempPassword();
        String encodedPassword = passwordEncodePort.encode(tempPassword);

        updatePasswordPort.updatePassword(command.email(), encodedPassword);
        emailCodePort.saveTempPassword(command.email(), EXPIRES_IN_SECONDS); // redis에 만료시간 저장
        emailSendPort.sendTempPassword(command.email(), tempPassword); // 이메일 발송
    }

    private String generateTempPassword() {
        return String.format("%08d", new Random().nextInt(100000000));
    }

}
