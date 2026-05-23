package com.wanted.momocity.auth.application.policy;

import com.wanted.momocity.auth.domain.repository.StudentSignupRepository;
import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import org.springframework.stereotype.Component;

@Component
public class SignupPolicy {

    private final StudentSignupRepository studentSignupRepository;

    public SignupPolicy(StudentSignupRepository studentSignupRepository) {
        this.studentSignupRepository = studentSignupRepository;
    }

    public void ensureEligible(String email){
        if(studentSignupRepository.existEmail(email)){
            throw new DomainRuleViolationException("이미 가입된 이메일입니다.");
        }
    }

}
