package com.wanted.momocity.viewing.presentation.api.response;

/*
* comment.
*  이어보기 시작 지점 반환
*  프론트가 lastPositionSec 받아서 영상 시작 시점 세팅
* */

public record ChapterResumeResponse(
        Long lectureId,
        Long chapterId,
        String chapterTitle,
        int lastPositionSec,
        int durationSec,
        int totalProgress
) {
}
