package com.wanted.momocity.teacher.application.command;

/*
 * 강사 신청 승인 명령.
 * Controller 가 HTTP 요청을 받아 이 Command 로 변환해 Application Service 에 전달.
 *
 * REF: module00-clean-architecture catalog/application/command/PublishCourseCommand.java
 */
public record ApproveTeacherCommand(
        Long userId
) {
}
