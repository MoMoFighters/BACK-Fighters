package com.wanted.momocity.auth.application.service;

import com.wanted.momocity.auth.application.command.EmailVerifyCommand;
import com.wanted.momocity.auth.application.port.EmailCodePort;
import com.wanted.momocity.auth.application.port.LoadUserPort;
import com.wanted.momocity.auth.application.usecase.AuthQueryUsecase;
import com.wanted.momocity.auth.domain.exception.InvalidVerificationCodeException;
import com.wanted.momocity.auth.domain.exception.UserNotFoundException;
import com.wanted.momocity.auth.domain.model.User;
import com.wanted.momocity.auth.presentation.api.response.LoginCompletedResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class AuthQueryService implements AuthQueryUsecase {

    private final EmailCodePort emailCodePort;
    private final LoadUserPort loadUserPort;

    // 이메일 인증
    @Override
    public void emailVerify(EmailVerifyCommand command) {
        String savedCode = emailCodePort.find(command.email());


        if (savedCode == null) { // 만료되었거나 존재하지 않는 경우
            log.warn("[email] 인증코드 만료 | email={}", command.email());
            throw new InvalidVerificationCodeException("인증 코드가 만료되었습니다. 재발송 버튼을 눌러 인증코드를 다시 발급받아 입력해주세요.");
        }

        if (!savedCode.equals(command.code())) { // 코드가 틀린 경우
            log.warn("[email] 인증코드 불일치 | email={}", command.email());
            throw new InvalidVerificationCodeException("인증 코드가 올바르지 않습니다.");
        }

        emailCodePort.delete(command.email());
        emailCodePort.saveVerified(command.email(), 180L);  // 인증 성공하면 그 값을 3분동안 유지하고
        log.info("[email] 이메일 인증 완료 | email={}", command.email());

    }


    // 로그인 후 정보 전달
    @Override
    public LoginCompletedResponse getInfo(String userId) {
        User loginUser = loadUserPort.findById(Long.parseLong(userId))
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));
        return new LoginCompletedResponse(loginUser.getRole(),loginUser.getIsTempPwd(),loginUser.getNickname());
    }
}
