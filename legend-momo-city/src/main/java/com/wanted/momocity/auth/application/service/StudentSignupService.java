package com.wanted.momocity.auth.application.service;

import com.wanted.momocity.auth.application.command.StudentSignupCommand;
import com.wanted.momocity.auth.application.policy.SignupPolicy;
import com.wanted.momocity.auth.application.usecase.StudentSignupUsecase;
import com.wanted.momocity.auth.domain.model.User;
import com.wanted.momocity.auth.domain.repository.UserRepository;

public class StudentSignupService implements StudentSignupUsecase {

    private final UserRepository userRepository;
    private final SignupPolicy signupPolicy;

    public StudentSignupService(UserRepository userRepository, SignupPolicy signupPolicy) {
        this.userRepository = userRepository;
        this.signupPolicy = signupPolicy;
    }

    @Override
    public void signup(StudentSignupCommand command) {

        // 이메일 중복 확인
        signupPolicy.ensureEligible(command.email());

        User newStudent = userRepository.register(User.studentRegister(command.email(), command.password(), command.name()));

    }
}
