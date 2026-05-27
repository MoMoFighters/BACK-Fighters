package com.wanted.momocity.calendar.application.service;

import com.wanted.momocity.calendar.domain.model.Calendar;
import com.wanted.momocity.calendar.domain.repository.CalendarRepository;
import com.wanted.momocity.calendar.presentation.api.response.DailyCalendarResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/*
* comment.
*  [테스트 전략]
*  @ExtendWith(MockitoExtension.class)
*  -> Spring 전체 콘텍스트를 로드하지 않고, Mockito 가 필요한 Mock 객체만 생성해서 주입
*  -> 가볍고 빠른 단위 테스트 가능
*  -
*  [테스트 대상]
*  GetDailyCalendarService.getDailyCalendar()
*  -> 날짜별 Calendar 조회 후 Todo / Memo 분리 반환 로직 검증
* */

@ExtendWith(MockitoExtension.class)
@DisplayName("GetDailyCalendarService 테스트")
public class getDailyCalendarServiceTest {

    /*
     * @Mock
     * - 실제 DB 에 접근하지 않고 가짜 CalendarRepository 를 생성
     * - when().thenReturn() 으로 원하는 반환값 지정 가능
     */

    @Mock
    private CalendarRepository calendarRepository;

    /*
     * @InjectMocks
     * - @Mock 으로 만든 가짜 객체를 GetDailyCalendarService 에 주입
     * - 실제 Service 로직만 독립적으로 테스트 가능
     */

    @InjectMocks
    private GetDailyCalendarService getDailyCalendarService;

    @Test
    @DisplayName("날짜별 조회 시 Todo, Memo 분리 반환 테스트")
    void 날짜별_조회_Todo_Memo_분리_테스트() {

        // given
        // 테스트에 사용할 userId, date 설정
        Long userId = 1L;
        LocalDate date = LocalDate.of(2026, 5, 27);

        // DB 에서 조회된 것처럼 reconstitute() 로 Calendar 객체 생성
        // -> create() 는 신규 생성용, reconstitute() 는 DB 복원용
        List<Calendar> mockCalendars = List.of(
                Calendar.reconstitute(
                        1L, userId, "모모파이터즈",
                        Calendar.Category.TODO,
                        date, null, false
                ),
                Calendar.reconstitute(
                        2L, userId, "생일",
                        Calendar.Category.MEMO,
                        date, date, false
                )
        );

        // when(메서드 호출).thenReturn(반환값 강제 지정)
        // -> findByUserIdAndDate() 가 호출되면 mockCalendars 반환
        // -> 실제 DB 접근 없이 원하는 데이터 주입 가능
        when(calendarRepository.findByUserIdAndDate(userId, date))
                .thenReturn(mockCalendars);

        // when
        // 실제 테스트 대상 메서드 호출
        DailyCalendarResponse response =
                getDailyCalendarService.getDailyCalendar(userId, date);

        // then
        // assertNotNull → response 자체가 null 이 아닌지 확인
        assertNotNull(response);
        // assertEquals → 기대값, 실제값 비교
        // Todo 1개, Memo 1개 분리됐는지 확인
        assertEquals(date, response.date());
        assertEquals(1, response.todos().size());   // Todo 1개
        assertEquals(1, response.memos().size());   // Memo 1개
    }

    @Test
    @DisplayName("해당 날짜 데이터 없으면 빈 배열 반환 테스트")
    void 날짜별_조회_데이터_없으면_빈_배열_반환_테스트() {

        // given
        Long userId = 1L;
        LocalDate date = LocalDate.of(2026, 5, 27);

        // List.of() → 빈 리스트 반환 강제화
        // -> 해당 날짜에 데이터 없는 상황 시뮬레이션
        when(calendarRepository.findByUserIdAndDate(userId, date))
                .thenReturn(List.of());

        // when
        DailyCalendarResponse response =
                getDailyCalendarService.getDailyCalendar(userId, date);

        // then
        assertNotNull(response);
        // 데이터 없을 때 404 가 아닌 빈 배열 반환하는지 확인
        // isEmpty() → 리스트가 비어있는지 확인
        assertTrue(response.todos().isEmpty());
        assertTrue(response.memos().isEmpty());
    }

}
