package com.wanted.momocity.lecture.presentation.api.request;

import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.momocity.lecture.application.command.AdminChangeLectureStatusCommand;
import com.wanted.momocity.lecture.domain.model.LectureStatus;

// 관리자 강의 상태 변경 요청 DTO
public record AdminChangeLectureStatusRequest(
        String lectureStatus // ACTIVE 또는 HOLD
) {

    /*
     * Controller에서 adminId, lectureId를 함께 받아 Command로 변환
     * adminId는 Authorization 토큰에서 가져온 사용자 ID
     * lectureId는 URL PathVariable애서 가져온다.
     */
    public AdminChangeLectureStatusCommand toCommand(
            Long adminId,
            Long lectureId
    ) {
        return new AdminChangeLectureStatusCommand(
                adminId,
                lectureId,
                parseLectureStatus(lectureStatus)
        );
    }

    // 문자열로 들어온 lectureStatus를 enum으로 변환
    private LectureStatus parseLectureStatus(String lectureStatus) {
        if (lectureStatus == null || lectureStatus.isBlank()) {
            throw new DomainRuleViolationException("강의 상태는 필수입니다.");
        }

        try {
            return LectureStatus.valueOf(lectureStatus.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new DomainRuleViolationException("허용되지 않은 강의 상태입니다.");
        }
    }
}