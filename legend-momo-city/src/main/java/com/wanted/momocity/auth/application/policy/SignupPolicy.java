package com.wanted.momocity.auth.application.policy;

import com.wanted.momocity.auth.application.port.EmailCodePort;
import com.wanted.momocity.auth.domain.exception.EmailNotVerifiedException;
import com.wanted.momocity.auth.domain.repository.UserRepository;
import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SignupPolicy {

    private final UserRepository userRepository;
    private final EmailCodePort emailCodePort;

    public void ensureEligible(String email){

        // 이메일 중복 확인
        if(userRepository.existsByEmail(email)){
            throw new DomainRuleViolationException("이미 가입된 이메일입니다.");
        }

        // 이메일 인증 여부 확인
        if (!emailCodePort.isVerified(email)) {
            throw new EmailNotVerifiedException("이메일 인증이 필요합니다.");
        }

    }

}
