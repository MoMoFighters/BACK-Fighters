package com.wanted.momocity.calendar.application.service;

import com.wanted.momocity.calendar.application.usecase.GetDailyCalendarUseCase;
import com.wanted.momocity.calendar.domain.model.Calendar;
import com.wanted.momocity.calendar.domain.repository.CalendarRepository;
import com.wanted.momocity.calendar.presentation.api.response.DailyCalendarResponse;
import com.wanted.momocity.calendar.presentation.api.response.MemoResponse;
import com.wanted.momocity.calendar.presentation.api.response.TodoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/*
* comment.
*  날빠별 캘린더 조회 UseCase 구현체
*  Todo 와 Memo 분리해서 반ㅎ
* */

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetDailyCalendarService implements GetDailyCalendarUseCase {

    private final CalendarRepository calendarRepository;

    @Override
    public DailyCalendarResponse getDailyCalendar(Long userId, LocalDate date) {

        // 날짜별 전체 조회 (Todo + Memo)
        List<Calendar> calendars = calendarRepository
                .findByUserIdAndDate(userId, date);

        // Todo 분리
        List<TodoResponse> todos = calendars.stream()
                .filter(c -> c.getCategory() == Calendar.Category.TODO)
                .map( c -> new TodoResponse(
                        c.getId(),
                        c.getTitle(),
                        c.getCategory(),
                        c.getStart(),
                        c.isCompleted()
                ))
                .toList();

        // Memo 분리
        List<MemoResponse> memos = calendars.stream()
                .filter(c -> c.getCategory() == Calendar.Category.MEMO)
                .map(c -> new MemoResponse(
                        c.getId(),
                        c.getTitle(),
                        c.getCategory(),
                        c.getStart(),
                        c.getEnd()
                ))
                .toList();

        return new DailyCalendarResponse(date, todos, memos);

    }
}
