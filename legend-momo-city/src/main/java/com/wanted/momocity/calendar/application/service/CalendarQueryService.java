package com.wanted.momocity.calendar.application.service;

import com.wanted.momocity.calendar.application.usecase.CalendarQueryUseCase;
import com.wanted.momocity.calendar.domain.model.Calendar;
import com.wanted.momocity.calendar.domain.repository.CalendarRepository;
import com.wanted.momocity.calendar.presentation.api.response.MemoResponse;
import com.wanted.momocity.calendar.presentation.api.response.MonthlyCalendarResponse;
import com.wanted.momocity.calendar.presentation.api.response.TodoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/*
 * comment.
 *  - 읽기 전용 UseCase 구현체
 *  - @Transactional(readOnly = true) 로 DB 부하 최소화
 *  - 상태 변경 없음, 조회만 담당
 *  -
 *  [담당 UseCase]
 *  - CalendarQueryUseCase : 월별 캘린더 조회
 */

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CalendarQueryService implements CalendarQueryUseCase {

    private final CalendarRepository calendarRepository;

    @Override
    public MonthlyCalendarResponse handle(Long userId, LocalDate startDate, LocalDate endDate) {

        // 해당 월 전체 조회 (Todo + Memo)
        List<Calendar> calendars = calendarRepository
                .findByUserIdAndDateBetween(userId, startDate, endDate);

        // Todo 분리
        List<TodoResponse> todos = calendars.stream()
                .filter(c -> c.getCategory() == Calendar.Category.TODO)
                .map(c -> new TodoResponse(
                        c.getId(), c.getTitle(),
                        c.getCategory(), c.getStart(), c.isCompleted()
                ))
                .toList();
        // Memo 분리
        List<MemoResponse> memos = calendars.stream()
                .filter(c -> c.getCategory() == Calendar.Category.MEMO)
                .map(c -> new MemoResponse(
                        c.getId(), c.getTitle(),
                        c.getCategory(), c.getStart(), c.getEnd()
                ))
                .toList();

        log.info("[Calendar] 월별 조회 완료 | userId={}, startDate={}, endDate={}, todoCount={}, memoCount={}",
                userId, startDate, endDate, todos.size(), memos.size());

        return new MonthlyCalendarResponse(startDate, endDate, todos, memos);
    }

}
