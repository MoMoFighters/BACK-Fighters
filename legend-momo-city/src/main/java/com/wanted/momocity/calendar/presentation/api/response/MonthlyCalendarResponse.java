package com.wanted.momocity.calendar.presentation.api.response;

import com.wanted.momocity.calendar.application.port.TodayChapterInfo;

import java.time.LocalDate;
import java.util.List;

public record MonthlyCalendarResponse(
        LocalDate startDate,
        LocalDate endDate,
        List<TodoResponse> todos,
        List<MemoResponse> memos,
        List<TodayChapterInfo> todayChapters
){
}
