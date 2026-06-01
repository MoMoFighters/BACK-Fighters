package com.wanted.momocity.enrollment.application.command;

public record CreateEnrollmentCommand(
        // 학생 Id
        Long studentId,
        // 강의 Id
        Long lectureId
) {
}
