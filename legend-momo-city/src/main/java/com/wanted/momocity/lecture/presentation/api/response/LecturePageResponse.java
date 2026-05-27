package com.wanted.momocity.lecture.presentation.api.response;

import java.util.List;

/**
 * LecturePageResponse는 강의 목록 페이지 응답 DTO
 *
 * content에는 강의 목록이 들어가고,
 * page, size, totalElements, totalPages는 페이징 정보를 나타냄
 */
public record LecturePageResponse(

        // 현재 페이지의 강의 목록
        List<LectureListItemResponse> content,

        // 현재 페이지 번호
        int page,

        // 한 페이지 크기
        int size,

        // 전체 강의 개수
        long totalElements,

        // 전체 페이지 수입니다.
        int totalPages

) {
}