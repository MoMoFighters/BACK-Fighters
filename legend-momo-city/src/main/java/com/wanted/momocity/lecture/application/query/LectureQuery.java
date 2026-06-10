package com.wanted.momocity.lecture.application.query;

import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.momocity.lecture.domain.model.LectureCategory;
import com.wanted.momocity.lecture.domain.model.LectureStatus;

/* comment
 * Lecture 조회 조건 Query record 모음.
 * 컨트롤러에서 받은 조회 조건을 application service로 넘길 때 사용한다.
 */
public final class LectureQuery {

    private LectureQuery() {
    }

    // 관리자가 강의 상세 정보를 조회할 때 사용하는 Query.
    public record GetAdminLectureDetailQuery(
            Long adminId,
            Long lectureId
    ) {
        public GetAdminLectureDetailQuery {
            if (adminId == null) {
                throw new DomainRuleViolationException("관리자 정보는 필수입니다.");
            }
            if (lectureId == null) {
                throw new DomainRuleViolationException("강의 ID는 필수입니다.");
            }
        }
    }

    // 관리자가 강의 목록을 조회할 때 사용하는 Query.
    public record GetAdminLecturesQuery(
            Long adminId,
            LectureStatus status,
            LectureCategory category,
            String keyword,
            int page,
            int size
    ) {
        public GetAdminLecturesQuery {
            if (adminId == null) {
                throw new DomainRuleViolationException("관리자 정보는 필수입니다.");
            }
            if (status != null
                    && status != LectureStatus.WAITING
                    && status != LectureStatus.ACTIVE) {
                throw new DomainRuleViolationException("관리자 강의 목록에서는 승인 대기 또는 진행 중 강의만 조회할 수 있습니다.");
            }
            validatePageAndSize(page, size);
        }
    }

    // 학생 기준 강의 목록을 조회할 때 사용하는 Query.
    public record GetLecturesQuery(
            Long userId,
            LectureCategory category,
            Boolean enrolled,
            String keyword,
            int page,
            int size
    ) {
        public GetLecturesQuery {
            validatePageAndSize(page, size);
        }
    }

    // 학생이 강의 상세 정보를 조회할 때 사용하는 Query.
    public record GetStudentLectureDetailQuery(
            Long userId,
            Long lectureId
    ) {
        public GetStudentLectureDetailQuery {
            if (userId == null || userId <= 0) {
                throw new DomainRuleViolationException("유효하지 않은 사용자입니다.");
            }
            if (lectureId == null || lectureId <= 0) {
                throw new DomainRuleViolationException("유효하지 않은 강의 식별자입니다.");
            }
        }
    }

    // 강사가 본인 강의 상세 정보를 조회할 때 사용하는 Query.
    public record GetTeacherLectureDetailQuery(
            Long teacherId,
            Long lectureId
    ) {
        public GetTeacherLectureDetailQuery {
            if (teacherId == null) {
                throw new DomainRuleViolationException("강사 정보는 필수입니다.");
            }
            if (lectureId == null) {
                throw new DomainRuleViolationException("강의 ID는 필수입니다.");
            }
        }
    }

    // 강사가 본인 강의 목록을 조회할 때 사용하는 Query.
    public record GetTeacherLecturesQuery(
            Long teacherId,
            int page,
            int size,
            LectureCategory category,
            String keyword
    ) {
        public GetTeacherLecturesQuery {
            if (teacherId == null) {
                throw new DomainRuleViolationException("강사 정보는 필수입니다.");
            }
            validatePageAndSize(page, size);
        }
    }

    private static void validatePageAndSize(int page, int size) {
        if (page < 1) {
            throw new DomainRuleViolationException("페이지 번호는 1 이상이어야 합니다.");
        }
        if (size < 1) {
            throw new DomainRuleViolationException("페이지 크기는 1 이상이어야 합니다.");
        }
    }
}