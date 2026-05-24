package com.wanted.momocity.auth.application.policy;

import com.wanted.momocity.auth.domain.repository.UserRepository;
import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import org.springframework.stereotype.Component;

@Component
public class SignupPolicy {

    private final UserRepository userRepository;

    public SignupPolicy(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void ensureEligible(String email){

        // 이메일 중복 확인
        if(userRepository.existsByEmail(email)){
            throw new DomainRuleViolationException("이미 가입된 이메일입니다.");
        }

        // 강사 파일 검증


    }

}
