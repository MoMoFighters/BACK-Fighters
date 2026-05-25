package com.wanted.momocity.auth.application.service;

import com.wanted.momocity.auth.application.command.EmailSendCommand;
import com.wanted.momocity.auth.application.port.EmailCodePort;
import com.wanted.momocity.auth.application.port.EmailSendPort;
import com.wanted.momocity.auth.application.port.LoadUserPort;
import com.wanted.momocity.auth.application.result.EmailSendResult;
import com.wanted.momocity.auth.application.usecase.EmailSendUsecase;
import com.wanted.momocity.auth.presentation.api.exception.DuplicateEmailException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@Service
@Transactional
@RequiredArgsConstructor
public class EmailSendService implements EmailSendUsecase{

    private final LoadUserPort loadUserPort;
    private final EmailCodePort emailCodePort;
    private final EmailSendPort emailSendPort;

    private static final long EXPIRES_IN_SECONDS = 180L; // 인증코드 만료시간 3분

    @Override
    public EmailSendResult emailSend(EmailSendCommand command) {
        if (loadUserPort.findByEmail(command.email()).isPresent()) {
            throw new DuplicateEmailException("이미 가입된 이메일입니다.");
        }

        String code = generateCode();
        emailCodePort.save(command.email(), code, EXPIRES_IN_SECONDS);
        emailSendPort.send(command.email(), code);

        return new EmailSendResult(EXPIRES_IN_SECONDS);
    }

    private String generateCode() {
        return String.format("%06d", new Random().nextInt(1000000));
    }
}
