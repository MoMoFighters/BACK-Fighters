package com.wanted.momocity.teacher.application.usecase;

import com.wanted.momocity.teacher.application.command.ApproveTeacherCommand;
import com.wanted.momocity.teacher.application.command.RejectTeacherCommand;

import java.time.Instant;

/* comment.
    TeacherApplicationCommandUseCase 정리
    해당 인터페이스가 하는 일 : Controller 가 강사 승인/반려 를 요청할 때 호출하는 진입점 약속
    위치 : teacher/application/usecase
    QueryUseCase 와 짝꿍 (CQRS) :
        - QueryUseCase : 조회 진입점 (목록/상세)
        - CommandUseCase : 변경 진입점 (승인/반려)
 */

public interface TeacherApplicationCommandUseCase {

    // 승인
    TeacherActionResult approve(ApproveTeacherCommand command);

    // 거절
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
