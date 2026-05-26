package com.wanted.momocity.teacher.application.service;

import com.wanted.momocity.teacher.application.command.ApproveTeacherCommand;
import com.wanted.momocity.teacher.application.command.RejectTeacherCommand;
import com.wanted.momocity.teacher.application.port.UserStatusUpdatePort;
import com.wanted.momocity.teacher.application.usecase.TeacherApplicationCommandUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/*
 * 강사 신청 승인/반려 유스케이스 구현.
 *
 * 흐름:
 *  1. Command 의 반려 사유 길이 검증 (최소 10자)
 *  2. UserStatusUpdatePort 를 통해 회원 영역에 상태 변경 요청
 *  3. 도메인 이벤트(TeacherApprovedEvent / TeacherRejectedEvent) 발행
 *  4. 처리 결과 반환
 *
 * REF: module00-clean-architecture catalog/application/service/CourseCommandService.java
 */
@Service
@Transactional
public class TeacherApplicationCommandService implements TeacherApplicationCommandUseCase {

    private final UserStatusUpdatePort userStatusUpdatePort;

    public TeacherApplicationCommandService(UserStatusUpdatePort userStatusUpdatePort) {
        this.userStatusUpdatePort = userStatusUpdatePort;
    }

    @Override
    public TeacherActionResult approve(ApproveTeacherCommand command) {
        // TODO m03: userStatusUpdatePort.approveTeacher(userId) + TeacherApprovedEvent 발행
        throw new UnsupportedOperationException("TODO: m03 우선순위 1 - 강사 승인 (MS-5)");
    }

    @Override
    public TeacherActionResult reject(RejectTeacherCommand command) {
        // TODO m03: reason 10자 검증 -> userStatusUpdatePort.rejectTeacher(userId, reason) + TeacherRejectedEvent 발행
        throw new UnsupportedOperationException("TODO: m03 우선순위 1 - 강사 반려 (MS-5)");
    }
}
