package com.wanted.momocity.enrollment.application.command;

public record CreateEnrollmentCommand(
        // 학생 Id
        String studentEmail,
        // 강의 Id
        Long lectureId
) {
}
