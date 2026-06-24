package com.wanted.momocity.viewing.presentation.api.response;

/*
* comment.
*  강의 전체 진척도 반환
* */

public record TotalProgressResponse(
        Long lectureId,
        int lectureProgress,
        int completedCount,
        int totalChapterCount
) {
}
