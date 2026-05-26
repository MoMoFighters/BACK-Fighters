package com.wanted.momocity.viewing.presentation.api.response;

/*
* comment.
*  진척도 저장 후 업데이트 된 상태 반환
*  프론트가 이 값으로 UI 진척도 바 업데이트
* */

public record SaveProgressResponse(
        Long chapterId,
        int watchedSeconds,
        int progressRate,
        boolean isCompleted,
        int totalProgress,
        int completedCount
) {
}
