package com.wanted.momocity.auth.application.service;

import com.wanted.momocity.auth.application.command.StudentSignupCommand;
import com.wanted.momocity.auth.application.policy.SignupPolicy;
import com.wanted.momocity.auth.application.usecase.StudentSignupCommandUsecase;
import com.wanted.momocity.auth.domain.model.User;
import com.wanted.momocity.auth.domain.repository.StudentSignupRepository;

public class StudentSignupCommandService implements StudentSignupCommandUsecase {

    private final StudentSignupRepository studentSignupRepository;
    private final SignupPolicy signupPolicy;

    public StudentSignupCommandService(StudentSignupRepository studentSignupRepository, SignupPolicy signupPolicy) {
        this.studentSignupRepository = studentSignupRepository;
        this.signupPolicy = signupPolicy;
    }

    @Override
    public void signup(StudentSignupCommand command) {

        // 이메일 중복 확인
        signupPolicy.ensureEligible(command.email());

        User newStudent = studentSignupRepository.register(User.createStudent(command.email(), command.password(), command.name()));

    }
}
