package com.wanted.momocity.calendar.application.port;

/*
* comment.
*  TodayChapterPort 의 반환 DTO
*  -> calendar 도메인이 viewing 도메인 직접 참조 방지
*  -> 강의 제목, 챕터 제목
* */

public record TodayChapterInfo(
        String lectureTitle,
        Long lectureId,
        String category,
        String chapterTitle
) {
}
