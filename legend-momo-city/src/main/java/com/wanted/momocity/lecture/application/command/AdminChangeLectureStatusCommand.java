package com.wanted.momocity.lecture.application.command;

import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.momocity.lecture.domain.model.LectureStatus;

// 관리자가 강의 상태를 변경할 때 사용하는 Command 객체
public record AdminChangeLectureStatusCommand(
        Long adminId,                   // 상태 변경을 요청한 관리자 ID
        Long lectureId,                 // 상태를 변경할 강의 ID
        LectureStatus lectureStatus     // 변경할 강의 상태
) {
    public AdminChangeLectureStatusCommand {
        validateAdminId(adminId);
        validateLectureId(lectureId);
        validateLectureStatus(lectureStatus);
    }

    // 관리자 ID는 인증된 사용자 정보에서 가져와야 하므로 필수
    private static void validateAdminId(Long adminId) {
        if (adminId == null) {
            throw new DomainRuleViolationException("관리자 정보는 필수입니다.");
        }
    }

    // 어떤 강의를 변경할지 알아야 하므로 강의 ID는 필수
    private static void validateLectureId(Long lectureId) {
        if (lectureId == null) {
            throw new DomainRuleViolationException("강의 ID는 필수입니다.");
        }
    }

    /*
     * 관리자 승인/거절 버튼에서는 ACTIVE, HOLD만 허용한다.
     * ACTIVE = 승인
     * HOLD = 거절
     */
    private static void validateLectureStatus(LectureStatus lectureStatus) {
        if (lectureStatus == null) {
            throw new DomainRuleViolationException("강의 상태는 필수입니다.");
        }

        if (lectureStatus != LectureStatus.ACTIVE
                && lectureStatus != LectureStatus.HOLD) {
            throw new DomainRuleViolationException("관리자는 강의를 승인 또는 거절 상태로만 변경할 수 있습니다.");
        }
    }
}