package com.wanted.momocity.calendar.application.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wanted.momocity.calendar.application.port.TodayChapterInfo;
import com.wanted.momocity.calendar.application.port.TodayChapterPort;
import com.wanted.momocity.calendar.application.usecase.CalendarQueryUseCase;
import com.wanted.momocity.calendar.domain.model.Calendar;
import com.wanted.momocity.calendar.domain.repository.CalendarRepository;
import com.wanted.momocity.calendar.presentation.api.response.DailyCalendarResponse;
import com.wanted.momocity.calendar.presentation.api.response.MemoResponse;
import com.wanted.momocity.calendar.presentation.api.response.MonthlyCalendarResponse;
import com.wanted.momocity.calendar.presentation.api.response.TodoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
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
@Slf4j
@Transactional(readOnly = true)
public class CalendarQueryService implements CalendarQueryUseCase {

    private final CalendarRepository calendarRepository;
    private final TodayChapterPort todayChapterPort;

    // String 타입 전용 RedisTemplate
    // 저장 / 조회 시 raw JSON 문자열 그대로 처리
    // GenericJackson2JsonRedisSerializer 의 @class 타입 정보 충돌 문제 없음
    private final StringRedisTemplate stringRedisTemplate;

    // activateDefaultTyping 비활성화 상태의 순수 ObjectMapper
    // @class 타입 정보 없이 순수 JSON 으로 직렬화 / 역직렬화
    // JavaTimeModule 등록으로 LocalDateTime 처리 가능
    // HTTP 응답 직렬화에는 영향 없음 (Spring 기본 ObjectMapper 와 별개)
    private final ObjectMapper plainObjectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    public CalendarQueryService(
            CalendarRepository calendarRepository,
            TodayChapterPort todayChapterPort,
            StringRedisTemplate stringRedisTemplate
    ) {
        this.calendarRepository = calendarRepository;
        this.todayChapterPort = todayChapterPort;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /*
    * comment.
    *  월별 캘린더 조회 (Memo)
    *  -
    *  캐시 키 : "calendar::{userId}:{year}:{month}"
    *  -
    *  캐시 조회 우선
    *  1. Redis 에서 키로 raw JSON 조회
    *  2. 캐시 히트 시 plainObjectMapper 로 PostListResponse 역직렬화 후 반환
    *  3. 캐시 미스 시 DB 조회 후 Redis 에 저장
    *  -
    *  예외처리
    *  - 캐시 조회 / 저장 실패 시 -> 로그만 남기고 DB 조회로 fallback
    *  -> 캐시 장애가 서비스 장애로 이어지지 않도록 방어
    * */

    // 월별 캘린더 조회
    @Override
    public MonthlyCalendarResponse handle(Long userId, LocalDate startDate, LocalDate endDate) {

        String cacheKey = "calendar::" + userId + ":" + startDate.getYear() + ":" + startDate.getMonthValue();

        // 1. Redis 캐시 조회
        try {
            String json = stringRedisTemplate.opsForValue().get(cacheKey);
            if (json != null) {
                MonthlyCalendarResponse cached = plainObjectMapper.readValue(
                        json, new TypeReference<MonthlyCalendarResponse>() {
                        }
                );
                log.debug("[Calendar] 월별 캐시 히트 | key={}", cacheKey);
                return cached;
            }
        } catch (Exception e) {
            log.warn("[Calendar] 월별 캐시 조회 실패, DB 조회로 fallback | key={} | 예외={}",
                    cacheKey, e.getMessage());
        }

        // 2. DB 조회
        List<Calendar> calendars = calendarRepository
                .findByUserIdAndDateBetween(userId, startDate, endDate);

        List<MemoResponse> memos = calendars.stream()
                .filter(c -> c.getCategory() == Calendar.Category.MEMO)
                .map(c -> new MemoResponse(
                        c.getId(), c.getTitle(),
                        c.getCategory(), c.getStart(), c.getEnd()
                ))
                .toList();

        MonthlyCalendarResponse response = new MonthlyCalendarResponse(startDate, endDate, memos);

        log.info("[Calendar] 월별 조회 완료 | userId={}, startDate={}, endDate={}, memoCount={}",
                userId, startDate, endDate, memos.size());

        // 3. Redis 캐시 저장
        // TTL 1시간 → Todo/Memo 작성/수정/삭제 시 캐시 무효화
        try {
            String json = plainObjectMapper.writeValueAsString(response);
            stringRedisTemplate.opsForValue().set(cacheKey, json, Duration.ofHours(1));
            log.debug("[Calendar] 월별 캐시 저장 | key={}", cacheKey);
        } catch (Exception e) {
            log.warn("[Calendar] 월별 캐시 저장 실패 | key={}", cacheKey);
        }

        return response;
    }

    // 일별 캘린더 조회
    // 오늘 수강 챕터 실시간 반영 필요
    @Override
    public DailyCalendarResponse handleDaily(Long userId, LocalDate date) {

        List<Calendar> calendars = calendarRepository
                .findByUserIdAndDateBetween(userId, date, date);

        // Todo
        List<TodoResponse> todos = calendars.stream()
                .filter(c -> c.getCategory() == Calendar.Category.TODO)
                .map(c -> new TodoResponse(
                        c.getId(), c.getTitle(),
                        c.getCategory(), c.getStart(), c.isCompleted()
                ))
                .toList();

                List<TodayChapterInfo> todayChapters =
                todayChapterPort.findTodayChapters(userId, date);

        log.info("[Calendar] 일별 조회 완료 | userId={}, date={}, todoCount={}, todayChapterCount={}",
                userId, date, todos.size(), todayChapters.size());

        return new DailyCalendarResponse(date, todos, todayChapters);

    }

}
