package com.wanted.momocity.teacher.application.service;

import com.wanted.momocity.teacher.application.command.ApproveTeacherCommand;
import com.wanted.momocity.teacher.application.command.RejectTeacherCommand;
import com.wanted.momocity.teacher.application.port.UserStatusUpdatePort;
import com.wanted.momocity.teacher.application.usecase.TeacherApplicationCommandUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/* comment.
    TeacherApplicationCommandUseCase 정리
    1. 해당 클래스가 하는 일 : TeacherApplicationCommandUseCase 인테페이스 실제 구현
    2. teacher/application/service
    3. QueryService 와의 차이 :
        - QueryService : @Transactional(readOnly=true) / UserQueryPort 의존 / 조회
        - CommandService : @Transactional (쓰기) / UserStatusUpdatePort 의존 / 변경
    4. 의존 흐름
    TeacherApplicationController → TeacherApplicationCommandUseCase (인터페이스)
    → TeacherApplicationCommandService (이 클래스) → UserStatusUpdatePort (외향 포트)
    → MemberUserAdapter → 회원 영역의 Member.approveAsTeacher / rejectAsTeacher
    5. m03 미구현 (해야할 일) :
        - approve : userStatusUpdatePort.approveTeacher 호출 + TeacherApprovedEvent 발행
        - reject : reason 10자 검증 → userStatusUpdatePort.rejectTeacher 호출 + TeacherRejectedEvent 발행
    6. 이벤트 발행 자리 : 이 서비스가 도메인 이벤트를 발행 (회원 영역이 아니라)
        - 회원 영역의 Member 가 상태 변경
        - 변경 후 강사 영역이 강사 승인됨/반려됨 이벤트 발행 (알림 영역이 수신)
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
