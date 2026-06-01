package com.wanted.momocity.lecture.application.query;

import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;

// 학생 강의 상세 조회 조건을 담는 Query 객체
public record GetStudentLectureDetailQuery(

        // Authorization 토큰에서 꺼낸 로그인 사용자 ID
        Long userId,

        // 상세 조회할 강의 ID
        Long lectureId

) {

    // record가 생성될 때 값이 정상인지 검사
    public GetStudentLectureDetailQuery {
        if (userId == null || userId <= 0) {
            throw new DomainRuleViolationException("유효하지 않은 사용자입니다.");
        }

        if (lectureId == null || lectureId <= 0) {
            throw new DomainRuleViolationException("유효하지 않은 강의 식별자입니다.");
        }
    }
}