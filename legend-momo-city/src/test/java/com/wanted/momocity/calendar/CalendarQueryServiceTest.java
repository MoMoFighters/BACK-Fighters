package com.wanted.momocity.calendar;

import com.wanted.momocity.calendar.application.service.CalendarQueryService;
import com.wanted.momocity.calendar.domain.model.Calendar;
import com.wanted.momocity.calendar.domain.repository.CalendarRepository;
import com.wanted.momocity.calendar.presentation.api.response.MonthlyCalendarResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/*
 * comment.
 *  CalendarQueryService 단위 테스트
 *  -
 *  [테스트 대상 UseCase]
 *  - CalendarQueryUseCase : 월별 캘린더 조회
 *  -
 *  [주요 예외 시나리오]
 *  1. 해당 월에 캘린더 데이터가 없는 경우 → 빈 리스트 반환
 *  2. 시작일이 종료일보다 늦은 경우 (역전된 날짜 범위) → 빈 리스트 반환
 *  3. userId 가 null 인 경우 → NullPointerException 발생
 *  4. Todo / Memo 카테고리가 정확히 분리되는지 검증
 */
class CalendarQueryServiceTest {

    private CalendarQueryService calendarQueryService;
    private CalendarRepository calendarRepository;

    @BeforeEach
    void setUp() {
        calendarRepository = mock(CalendarRepository.class);
        calendarQueryService = new CalendarQueryService(calendarRepository);
    }

    @Test
    void 해당_월에_데이터가_없으면_빈_리스트를_반환한다() {

        // given
        Long userId = 1L;
        LocalDate startDate = LocalDate.of(2025, 6, 1);
        LocalDate endDate = LocalDate.of(2025, 6, 30);

        when(calendarRepository.findByUserIdAndDateBetween(userId, startDate, endDate))
                .thenReturn(Collections.emptyList());

        // when
        MonthlyCalendarResponse response = calendarQueryService.handle(userId, startDate, endDate);

        // then
        assertTrue(response.todos().isEmpty());
        assertTrue(response.memos().isEmpty());
    }

    @Test
    void 날짜_범위가_역전된_경우_빈_리스트를_반환한다() {

        // given : 시작일이 종료일보다 늦음
        Long userId = 1L;
        LocalDate startDate = LocalDate.of(2025, 6, 30);
        LocalDate endDate = LocalDate.of(2025, 6, 1);

        when(calendarRepository.findByUserIdAndDateBetween(userId, startDate, endDate))
                .thenReturn(Collections.emptyList());

        // when
        MonthlyCalendarResponse response = calendarQueryService.handle(userId, startDate, endDate);

        // then
        assertTrue(response.todos().isEmpty());
        assertTrue(response.memos().isEmpty());
    }

    @Test
    void userId가_null인_경우_예외가_발생한다() {

        // given
        Long userId = null;
        LocalDate startDate = LocalDate.of(2025, 6, 1);
        LocalDate endDate = LocalDate.of(2025, 6, 30);

        when(calendarRepository.findByUserIdAndDateBetween(null, startDate, endDate))
                .thenThrow(new NullPointerException("userId는 null일 수 없습니다."));

        // when & then
        assertThrows(
                NullPointerException.class,
                () -> calendarQueryService.handle(userId, startDate, endDate)
        );
    }

    @Test
    void Todo와_Memo가_카테고리별로_정확히_분리된다() {

        // given
        Long userId = 1L;
        LocalDate startDate = LocalDate.of(2025, 6, 1);
        LocalDate endDate = LocalDate.of(2025, 6, 30);

        Calendar todo = mock(Calendar.class);
        when(todo.getCategory()).thenReturn(Calendar.Category.TODO);
        when(todo.getId()).thenReturn(1L);
        when(todo.getTitle()).thenReturn("운동하기");
        when(todo.getStart()).thenReturn(startDate);
        when(todo.isCompleted()).thenReturn(false);

        Calendar memo = mock(Calendar.class);
        when(memo.getCategory()).thenReturn(Calendar.Category.MEMO);
        when(memo.getId()).thenReturn(2L);
        when(memo.getTitle()).thenReturn("독서 메모");
        when(memo.getStart()).thenReturn(startDate);
        when(memo.getEnd()).thenReturn(endDate);

        when(calendarRepository.findByUserIdAndDateBetween(userId, startDate, endDate))
                .thenReturn(List.of(todo, memo));

        // when
        MonthlyCalendarResponse response = calendarQueryService.handle(userId, startDate, endDate);

        // then
        assertEquals(1, response.todos().size());
        assertEquals(1, response.memos().size());
        assertEquals("운동하기", response.todos().get(0).title());
        assertEquals("독서 메모", response.memos().get(0).title());
    }

    @Test
    void Todo만_있는_경우_Memo는_빈_리스트를_반환한다() {

        // given
        Long userId = 1L;
        LocalDate startDate = LocalDate.of(2025, 6, 1);
        LocalDate endDate = LocalDate.of(2025, 6, 30);

        Calendar todo = mock(Calendar.class);
        when(todo.getCategory()).thenReturn(Calendar.Category.TODO);
        when(todo.getId()).thenReturn(1L);
        when(todo.getTitle()).thenReturn("운동하기");
        when(todo.getStart()).thenReturn(startDate);
        when(todo.isCompleted()).thenReturn(false);

        when(calendarRepository.findByUserIdAndDateBetween(userId, startDate, endDate))
                .thenReturn(List.of(todo));

        // when
        MonthlyCalendarResponse response = calendarQueryService.handle(userId, startDate, endDate);

        // then
        assertEquals(1, response.todos().size());
        assertTrue(response.memos().isEmpty());
    }
}