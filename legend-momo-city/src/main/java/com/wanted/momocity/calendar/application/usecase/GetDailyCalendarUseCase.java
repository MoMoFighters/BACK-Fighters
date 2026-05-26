package com.wanted.momocity.calendar.application.usecase;

/*
* comment.
*  날짜별 캘린더 조회 (Todo  + Memo)
* */

import com.wanted.momocity.calendar.presentation.api.response.DailyCalendarResponse;

import java.time.LocalDate;

public interface GetDailyCalendarUseCase {

    DailyCalendarResponse getDailyCalendar(Long userId, LocalDate date);

}
