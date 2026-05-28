package com.wanted.momocity.lecture.presentation.api.response;

import java.util.List;

// 강사 강의 목록 페이지 응답 DTO
public record TeacherLecturePageResponse(
        // 강의 목록 여러개를 담기 위해 List 사용
        List<TeacherLectureListItemResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
