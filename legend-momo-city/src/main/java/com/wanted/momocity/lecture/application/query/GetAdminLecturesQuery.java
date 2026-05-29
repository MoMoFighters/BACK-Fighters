package com.wanted.momocity.lecture.application.query;

import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.momocity.lecture.domain.model.LectureCategory;
import com.wanted.momocity.lecture.domain.model.LectureStatus;

// 관리자가 강의 목록을 조회할 때 사용하는 조건을 담는 Query 객체
public record GetAdminLecturesQuery(
        Long adminId,
        LectureStatus status,
        LectureCategory category,
        String keyword,
        int page,
        int size
) {
    public GetAdminLecturesQuery {
        validateAdminId(adminId);
        validateStatus(status);
        validatePage(page);
        validateSize(size);
    }

    // 로그인한 관리자 id가 있는지 확인
    private static void validateAdminId(Long adminId) {
        if (adminId == null) {
            throw new DomainRuleViolationException("관리자 정보는 필수입니다.");
        }
    }

    // 로그인한 관리자 id가 있는지 확인
    // 관리자는 WAITING 또는 ACTIVE 상태의 강의만 목록에서 조회
    // status가 null이면 WAITING + ACTIVE 전체 조회로 처리
    private static void validateStatus(LectureStatus status) {
        if (status == null) {
            return;
        }

        if (status != LectureStatus.WAITING && status != LectureStatus.ACTIVE) {
            throw new DomainRuleViolationException("관리자 강의 목록에서는 승인 대기 또는 진행 중 강의만 조회할 수 있습니다.");
        }
    }

    // 프론트 페이지 번호는 1부터 시작한다.
    private static void validatePage(int page) {
        if (page < 1) {
            throw new DomainRuleViolationException("페이지 번호는 1 이상이어야 합니다.");
        }
    }

    // 한 페이지에 조회할 강의 개수는 1개 이상이어야 한다.
    private static void validateSize(int size) {
        if (size < 1) {
            throw new DomainRuleViolationException("페이지 크기는 1 이상이어야 합니다.");
        }
    }

}
