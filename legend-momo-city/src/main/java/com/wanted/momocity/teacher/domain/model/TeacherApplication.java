package com.wanted.momocity.teacher.domain.model;

import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;

import java.time.LocalDate;
import java.time.LocalDateTime;

/*
 * TeacherApplication 은 강사 영역(teacher BC)의 조회용 도메인 값 객체다.
 *
 * user 테이블에서 role='TEACHER', status='PENDING' 인 행을
 * "강사 신청서" 시각으로 표현한다.
 *
 * 책임:
 *  - 강사 신청자 정보를 읽기 전용으로 표현 (값 객체)
 *  - 행위(승인/반려) 없음. 실제 상태 변경은 회원 영역의 Member 도메인이 담당.
 *
 * REF: module00-clean-architecture catalog/domain/model/Course.java (도메인 모델 패턴 참고)
 *
 * 메모(노션 MS-4): ERD 에 신청일 컬럼 없음 → updated_at 을 appliedAt 대용으로 사용.
 */
public record TeacherApplication(
        Long userId,
        String email,
        String name,
        String nickname,
        LocalDate birth,
        String profileImageUrl,
        String category,
        String proof,
        LocalDateTime appliedAt
) {
    public TeacherApplication {
        if (userId == null) {
            throw new DomainRuleViolationException("강사 신청서 식별자(userId)는 필수입니다.");
        }
    }
}
