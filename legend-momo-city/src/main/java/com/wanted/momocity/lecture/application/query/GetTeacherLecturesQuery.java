package com.wanted.momocity.lecture.application.query;

import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.momocity.lecture.domain.model.LectureCategory;

// 강사 강의 목록 조회의 조건을 담은 record
public record GetTeacherLecturesQuery(
        Long teacherId,
        int page,
        int size,
        LectureCategory category,
        String keyword
) {
    // Query 객체가 만들어질 때 기본 조회 조건을 검증
    public GetTeacherLecturesQuery {
        validateTeacherId(teacherId);
        validatePage(page);
        validateSize(size);
    }
    // 로그인한 강사 email
    private static void validateTeacherId(Long teacherId) {
        if (teacherId == null) {
            throw new DomainRuleViolationException("강사 정보는 필수입니다.");
        }
    }

    // 페이지는 1이상
    private static void validatePage(int page) {
        if (page < 1) {
            throw new DomainRuleViolationException("페이지 번호는 0 이상이어야 합니다.");
        }
    }

    // size는 한 페이지에 조회할 강의 개수
    private static void validateSize(int size) {
        if (size < 1) {
            throw new DomainRuleViolationException("페이지 크기는 1 이상이어야 합니다.");
        }
    }

}
