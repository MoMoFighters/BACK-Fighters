package com.wanted.momocity.viewing.application.port;

/*
* comment.
*  특정 강의의 챕터별 진척도 Info
* */

public record ChapterProgressInfo(
        Long chapterId,
        int ProgressRate,
        boolean isCompleted,
        boolean isAccessible
) {
}
