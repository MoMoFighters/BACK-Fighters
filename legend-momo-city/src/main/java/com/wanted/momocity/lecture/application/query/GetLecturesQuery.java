package com.wanted.momocity.lecture.application.query;

import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.momocity.lecture.domain.model.LectureCategory;

// GetLecturesQuery는 강의 목록 조회 조건을 담는 객체
// Controller가 받은 조회 조건을 Service로 넘기는 상자
public record GetLecturesQuery(

        // Authorization 토큰에서 꺼낸 로그인 사용자 email
        Long userId,

        // 강의 카테고리 필터
        // 값이 없으면 전체 카테고리를 조회
        LectureCategory category,

        // 수강 신청 여부 필터
        // true면 내가 신청한 강의만,
        // false면 내가 신청하지 않은 강의만,
        // null이면 수강 신청 여부와 상관없이 조회
        Boolean enrolled,

        // 강의명 또는 강사명 검색어
        // 값이 없으면 검색 조건 없이 조회
        String keyword,

        // 조회할 페이지 번호
        // page는 1부터 시작
        int page,

        // 한 페이지에 조회할 강의 개수
        int size

) {

    /* comment
     * compact constructor -> 생성자를 풀지 않음 즉, 필드를 다시 적지 않고 짧게 쓸 수 있음
     * record가 생성될 때 page, size 값이 정상인지 검사
     */
    public GetLecturesQuery {
        if (page < 1) {
            throw new DomainRuleViolationException("페이지 번호는 0 이상이어야 합니다.");
        }

        if (size < 1) {
            throw new DomainRuleViolationException("페이지 크기는 1 이상이어야 합니다.");
        }
    }
}