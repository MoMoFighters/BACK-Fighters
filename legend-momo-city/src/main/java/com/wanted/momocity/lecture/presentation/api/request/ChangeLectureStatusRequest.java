package com.wanted.momocity.lecture.presentation.api.request;

import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.momocity.lecture.application.command.ChangeLectureStatusCommand;
import com.wanted.momocity.lecture.domain.model.LectureStatus;
import jakarta.validation.constraints.NotBlank;

// 강의 상태 변경 요청 DTO
public record ChangeLectureStatusRequest(

        // 변경할 강의 상태입니다. 예: ACTIVE, HOLD, DELETED, WAITING
        @NotBlank(message = "강의 상태는 필수입니다.")
        String lectureStatus
) {

    // 문자열로 받은 상태값을 LectureStatus enum으로 변환한 뒤 Command로 만든다.
    public ChangeLectureStatusCommand toCommand(
            Long teacherId,
            Long lectureId
    ) {
        return new ChangeLectureStatusCommand(
                teacherId,
                lectureId,
                parseLectureStatus()
        );
    }

    /* comment
     * 요청 상태값을 LectureStatus enum으로 변환
     * 허용되지 않은 값이면 400으로 처리될 도메인 예외를 던집니다.
     */
    private LectureStatus parseLectureStatus() {
        try {
            return LectureStatus.valueOf(lectureStatus.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new DomainRuleViolationException("허용되지 않은 강의 상태입니다.");
        }
    }
}