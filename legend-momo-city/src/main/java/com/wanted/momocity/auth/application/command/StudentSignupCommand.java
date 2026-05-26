package com.wanted.momocity.auth.application.command;

public record StudentSignupCommand(

        // 학생 회원가입 할 때
        String email,
        String password,
        String name
) {
}
