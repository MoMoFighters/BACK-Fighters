package com.wanted.momocity.lecture.presentation.api.response;

import java.util.List;

/*
 * 학생 강의 목록 조회의 data 영역을 담당하는 응답 DTO
 * content 배열과 페이지 정보를 함께 내려준다.
 */
public record StudentLecturePageResponse(
        List<StudentLectureListItemResponse> content, // 강의 목록 배열
        int page,                                     // 현재 페이지 번호
        int size,                                     // 한 페이지에 보여줄 강의 개수
        long totalElements,                          // 전체 강의 개수
        int totalPages                               // 전체 페이지 수
) {
}