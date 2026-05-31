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

    /*
     * comment.
     *  watchedSeconds:
     *  -> 실제로 본 최대 위치
     *  -> 뒤로 감기 시 감소 없음
     *  -> 앞으로 당기기 10초 초과 시 반영 안 함
     *  ->durationSec 초과 불가 (Math.min 처리)
     *  -
     *  progressRate:
     *  -> watchedSeconds / durationSec * 100
     */

    public record ChapterProgressItem(
            Long chapterId,
            String title,
            int orderNo,
            int watchedSeconds,
            int durationSec,
            int progressRate,
            boolean isCompleted,
            boolean isAccessible
    ) {}

}
