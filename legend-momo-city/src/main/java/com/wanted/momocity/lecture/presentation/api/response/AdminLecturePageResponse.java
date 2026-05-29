package com.wanted.momocity.lecture.presentation.api.response;

import java.util.List;

// 관리자 강의 목록 페이지 응답 DTO
public record AdminLecturePageResponse(
        List<AdminLectureListItemResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}