package com.wanted.momocity.teacher.application.command;

/*
 * 강사 신청 반려 명령.
 *
 * 반려 사유(reason)는 최소 10자 이상이어야 한다.
 * 검증은 Application Service 단계에서 수행한다.
 *
 * REF: module00-clean-architecture catalog/application/command/PublishCourseCommand.java
 */
public record RejectTeacherCommand(
        Long userId,
        String reason
) {
}
