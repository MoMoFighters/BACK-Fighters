package com.wanted.momocity.teacher.application.usecase;

import com.wanted.momocity.teacher.application.command.ApproveTeacherCommand;
import com.wanted.momocity.teacher.application.command.RejectTeacherCommand;

import java.time.Instant;

/*
 * 강사 신청 승인/반려 유스케이스.
 *
 * 구현체: TeacherApplicationCommandService
 * 호출자: TeacherApplicationController (MS-5)
 *
 * REF: module00-clean-architecture catalog/application/usecase/CourseCommandUseCase.java
 */
public interface TeacherApplicationCommandUseCase {

    TeacherActionResult approve(ApproveTeacherCommand command);

    TeacherActionResult reject(RejectTeacherCommand command);

    /**
     * 승인/반려 처리 결과.
     * status: ACTIVE (승인) 또는 REJECTED (반려)
     * reason: 반려 시 사유, 승인 시 null
     */
    record TeacherActionResult(
            Long userId,
            String status,
            String reason,
            Instant processedAt
    ) {
    }
}
