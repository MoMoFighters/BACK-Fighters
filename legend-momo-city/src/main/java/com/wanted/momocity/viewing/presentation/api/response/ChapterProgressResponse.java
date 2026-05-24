package com.wanted.momocity.viewing.presentation.api.response;

import java.util.List;

/*
* comment.
*  ChapterProgressResponse.ChapterProgressItem 으로 접근
*  -> ChapterProgressResponse 에서만 쓰는 아이템 모델이라 안에 묶음
*  -
*  챕터별 진척도 목록 반환
* */

public record ChapterProgressResponse(
        Long lectureId,
        List<ChapterProgressItem> chapters
) {
    public record ChapterProgressItem(
            Long chapterId,
            String title,
            int orderNo,
            int watchedSeconds,
            int durationSec,
            int progressRate,
            boolean isCompleted
    ) {}

}
