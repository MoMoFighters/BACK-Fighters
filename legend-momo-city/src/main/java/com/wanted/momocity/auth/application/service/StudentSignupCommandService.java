package com.wanted.momocity.auth.application.service;

import com.wanted.momocity.auth.application.command.StudentSignupCommand;
import com.wanted.momocity.auth.application.usecase.StudentSignupCommandUsecase;
import com.wanted.momocity.auth.domain.model.User;
import com.wanted.momocity.auth.domain.repository.StudentSignupRepository;

public class StudentSignupCommandService implements StudentSignupCommandUsecase {

    private final StudentSignupRepository studentSignupRepository;

    public StudentSignupCommandService(StudentSignupRepository studentSignupRepository) {
        this.studentSignupRepository = studentSignupRepository;
    }

    @Override
    public void signup(StudentSignupCommand command) {

        User newStudent = studentSignupRepository.register(User.createStudent(command.email(), command.password(), command.name()));

    }
}
