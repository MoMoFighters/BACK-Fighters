package com.wanted.momocity.lecture.application.query;

import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;

// 관리자가 강의 상세 정보를 조회할 때 사용하는 Query 객체
public record GetAdminLectureDetailQuery(
        Long adminId,
        Long lectureId
) {
    public GetAdminLectureDetailQuery {
        validateAdminId(adminId);
        validateLectureId(lectureId);
    }

    // 로그인한 관리자 id가 있는지 확인한다.
    private static void validateAdminId(Long adminId) {
        if (adminId == null) {
            throw new DomainRuleViolationException("관리자 정보는 필수입니다.");
        }
    }

    // 조회할 강의 id가 있는지 확인한다.
    private static void validateLectureId(Long lectureId) {
        if (lectureId == null) {
            throw new DomainRuleViolationException("강의 ID는 필수입니다.");
        }
    }
}