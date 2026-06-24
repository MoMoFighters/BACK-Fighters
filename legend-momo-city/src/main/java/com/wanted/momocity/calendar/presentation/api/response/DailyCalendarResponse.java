package com.wanted.momocity.calendar.presentation.api.response;

import com.wanted.momocity.calendar.application.port.TodayChapterInfo;

import java.time.LocalDate;
import java.util.List;

/*
* comment.
*  일별 캘린더 조회 응답 DTO
*  - date : 조회 날짜,
*  - todos : 해당 날짜 Todo 목록
*  - todayChapters : 해당 날짜 수강한 챕터 목록
* */

public record DailyCalendarResponse(
        LocalDate date,
        List<TodoResponse> todos,
        List<TodayChapterInfo> todayChapters
) {
}
