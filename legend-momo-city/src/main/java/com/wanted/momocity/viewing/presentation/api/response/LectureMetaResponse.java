package com.wanted.momocity.viewing.presentation.api.response;

/*
* comment.
*  강의 메타데이터 (플레이어 UI 상단용)
*  learning_history 에서 현재 챕터 조회 후 반환
*  -> 강의 기본 정보 + 챕터 리스트 + 현재 챕터
* */

import java.util.List;

public record LectureMetaResponse(
        Long lectureId,
        String lectureTitle,
        String thumbnailUrl,
        int totalChapterCount,
        Long currentChapterId,
        int currentChapterNo,
        String currentChapterTitle,
        List<ChapterItem> chapters
) {

    /*
    * comment.
    *  ChapterItem : 챕터 기본 정보 + 진척률 포함
    * */

    public record ChapterItem(
            Long chapterId,
            String title,
            int orderNo,
            int durationSec,
            int progressRate,
            boolean isCompleted,
            boolean isAccessible
    ) {}

}
