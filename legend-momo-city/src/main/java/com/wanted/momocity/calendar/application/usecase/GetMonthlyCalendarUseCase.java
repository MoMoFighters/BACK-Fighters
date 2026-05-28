package com.wanted.momocity.calendar.application.usecase;

import com.wanted.momocity.calendar.presentation.api.response.MonthlyCalendarResponse;

import java.time.LocalDate;

/*
* comment.
*  월별 캘린더 조회 UseCase
*  -> 해당 월 전체 Todo/Memo 반환
* */

public interface GetMonthlyCalendarUseCase {

    MonthlyCalendarResponse getMonthlyCalendar(
            Long userId, LocalDate startDate, LocalDate endDate
    );

}
