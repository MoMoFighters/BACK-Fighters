package com.wanted.momocity.user.application.command;

/* comment.
    ApproveTeacherCommand 정리
    1. 해당 클래스가 하는 일
        - 강사 승인을 처리해 달라는 요청 데이터 묶음
    2. 위치 : teacher/application/command (응용 계층 - 명령 폴더)
    3. 흐름 :
        Controller (HTTP 요청 받음) → TeacherActionRequest (HTTP DTO)
        → ApproveTeacherCommand 로 변환 (Controller 가 변환)
        → TeacherApplicationCommandUseCase.approve(command) 호출
        → 비즈니스 처리
    4. command 와 request 차이점
        - Request (TeacherActionRequest): HTTP 본문 직접 매핑. 표현 계층 책임
        - Command (이 파일): 응용 계층의 *순수 명령*. HTTP 모름
        - Controller 가 둘을 *변환* 함
    5. 필드 1개 (userId) 인 이유 : 승인은 *추가 데이터 필요 없음*. userId 만 있으면 끝
 */

public record ApproveTeacherCommand(
        Long userId // 누구인지만 파악하면 된다.
) {
}
