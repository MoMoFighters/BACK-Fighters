package com.wanted.momocity.user.application.command;

/* comment.
    RejectTeacherCommand 정리
    1. 해당 클래스가 하는 일 : 강사 반려 처리해 달라 요청하는 데이터 묶음
    2. 위치 : teacher/application/command
    3. ApproveTeacherCommand 와 차이 : reason(반려 사유) 필드 1개 추가
    4. 흐름 :
        Controller (HTTP 요청) → TeacherActionRequest
        → RejectTeacherCommand (Controller 가 변환)
        → TeacherApplicationCommandUseCase.reject(command)
        → 비즈니스 처리 (reason 검증 + 회원 영역 호출 + 이벤트 발행)
 */

public record RejectTeacherCommand(
        Long userId, // 반려할 신청자 PK
        String reason // 반려 사유
) {
}
