package com.wanted.momocity.calendar.application.usecase;

import com.wanted.momocity.calendar.presentation.api.response.MonthlyCalendarResponse;

import java.time.LocalDate;

/*
 * comment.
 *  Calendar 컨텍스트의 읽기 작업 전용 UseCase
 *  → 상태를 변경하지 않는 조회 작업만 담당
 *  → @Transactional(readOnly = true) 적용 가능
 *  -
 *  [담당 조회]
 *  - 월별 캘린더 조회 : 해당 월 전체 Todo/Memo 반환
 */

public interface CalendarQueryUseCase {

    MonthlyCalendarResponse handle(Long userId, LocalDate startDate, LocalDate endDate);

}
