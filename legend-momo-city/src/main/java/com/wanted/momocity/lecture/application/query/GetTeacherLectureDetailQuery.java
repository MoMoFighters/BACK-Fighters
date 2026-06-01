package com.wanted.momocity.lecture.application.query;

import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;

public record GetTeacherLectureDetailQuery(

        // 토큰에서 꺼낸 로그인 사용자 PK
        Long teacherId,
        Long lectureId
) {

    // Query 객체가 생성될 때 필요한 값이 비어있는지 검증
    public GetTeacherLectureDetailQuery {
        if (teacherId == null) {
            throw new DomainRuleViolationException("강사 정보는 필수입니다.");
        }

        if (lectureId == null) {
            throw new DomainRuleViolationException("강의 ID는 필수입니다.");
        }
    }
}
