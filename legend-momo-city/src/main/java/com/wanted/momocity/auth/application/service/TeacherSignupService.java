package com.wanted.momocity.auth.application.service;

import com.wanted.momocity.auth.application.command.TeacherSignupCommand;
import com.wanted.momocity.auth.application.policy.SignupPolicy;
import com.wanted.momocity.auth.application.usecase.TeacherSignupUseCase;
import com.wanted.momocity.auth.domain.model.User;
import com.wanted.momocity.auth.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class TeacherSignupService implements TeacherSignupUseCase {

    private final UserRepository userRepository;
    private final SignupPolicy signupPolicy;
    private final PasswordEncoder passwordEncoder;


    @Override
    public void signup(TeacherSignupCommand command) {

        // 이메일 중복 확인
        signupPolicy.ensureEligible(command.email());

        // 이메일(id), 비밀번호, 이름, 카테고리, 증빙자료 url 넘겨서 새로운 강사 자바 객체 생성
        userRepository.register(User.teacherRegister(command.email(), passwordEncoder.encode(command.password()), command.name(), command.category(),command.proof()));

    }
}
