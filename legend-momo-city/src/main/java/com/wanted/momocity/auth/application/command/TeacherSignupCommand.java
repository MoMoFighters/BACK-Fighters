package com.wanted.momocity.auth.application.command;

import com.wanted.momocity.auth.domain.model.Category;

public record TeacherSignupCommand(

        // 강사 회원가입 할 때
        String email,
        String password,
        String name,
        Category category,
        String proof // 증빙자료 url

) {
}
