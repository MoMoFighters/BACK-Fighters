package com.wanted.momocity.calendar.application.usecase;

import com.wanted.momocity.calendar.presentation.api.response.DailyCalendarResponse;

import java.time.LocalDate;

/*
 * comment.
 *  날짜별 캘린더 조회 (Todo  + Memo)
 * */


public interface GetDailyCalendarUseCase {

    DailyCalendarResponse getDailyCalendar(Long userId, LocalDate date);

}
