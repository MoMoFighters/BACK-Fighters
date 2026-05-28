package com.wanted.momocity.lecture.presentation.api.response;

import java.util.List;

/**
 * 학생 강의 목록 페이지 응답 DTO입니다.
 *
 * content에는 학생 강의 목록용 응답 객체들이 들어갑니다.
 */
public record StudentLecturePageResponse(

        // 현재 페이지의 강의 목록입니다.
        List<StudentLectureListItemResponse> content,

        // 현재 페이지 번호입니다.
        int page,

        // 한 페이지에 보여줄 강의 개수입니다.
        int size,

        // 전체 강의 개수입니다.
        long totalElements,

        // 전체 페이지 수입니다.
        int totalPages

) {
}