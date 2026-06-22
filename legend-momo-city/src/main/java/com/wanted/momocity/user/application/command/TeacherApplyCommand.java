package com.wanted.momocity.user.application.command;

import com.wanted.momocity.global.domain.model.Category;

public record TeacherApplyCommand(

        // 강사 신청 할 때
        Long userId,
        String nickname,
        Category category,
        String proof // 증빙자료 url

) {
}
