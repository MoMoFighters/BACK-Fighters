package com.wanted.momocity.viewing.presentation.api.response;

/*
* comment.
*  강의 메타데이터 (플레이어 UI 상단용)
*  learning_history 에서 현재 챕터 조회 후 반환
* */

public record LectureMetaResponse(
        Long lectureId,
        String lectureTitle,
        String instructorName,
        int totalChapterCount,
        int currentChapterNo,
        Long currentChapterId,
        String currentChapterTitle
) {
}
