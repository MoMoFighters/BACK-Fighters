package com.wanted.momocity.auth.application.service;

import com.wanted.momocity.auth.application.command.StudentSignupCommand;
import com.wanted.momocity.auth.application.policy.SignupPolicy;
import com.wanted.momocity.auth.application.port.EmailCodePort;
import com.wanted.momocity.auth.application.usecase.StudentSignupUsecase;
import com.wanted.momocity.auth.domain.model.User;
import com.wanted.momocity.auth.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class StudentSignupService implements StudentSignupUsecase {

    private final UserRepository userRepository;
    private final SignupPolicy signupPolicy;
    private final PasswordEncoder passwordEncoder;
    private final EmailCodePort emailCodePort;

    @Override
    public void signup(StudentSignupCommand command) {

        // 정책 확인
        signupPolicy.ensureEligible(command.email());

        // 이메일(id), 비밀번호, 이름 넘겨서 새로운 학생 자바 객체 생성
        userRepository.register(User.studentRegister(command.email(), passwordEncoder.encode(command.password()), command.name()));

        // 이메일 인증 하고서 인증됨 의 상태를 지움
        emailCodePort.deleteVerified(command.email());

    }
}
