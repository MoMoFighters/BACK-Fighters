package com.wanted.momocity.lecture.domain.model;

import java.util.List;

/* comment
 * LecturePage는 강의 목록 조회 결과와 페이징 정보를 담는 도메인
 * Spring Data의 Page를 application 계층 밖으로 직접 노출하지 않기 위해 사용
 */
public record LecturePage(

        // 현재 페이지의 강의 목록
        List<LectureAggregate> content,

        // 전체 강의 개수
        long totalElements,

        // 전체 페이지 수
        int totalPages

) {
}