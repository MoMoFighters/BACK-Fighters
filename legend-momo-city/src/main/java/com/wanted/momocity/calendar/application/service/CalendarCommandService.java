package com.wanted.momocity.calendar.application.service;

import com.wanted.momocity.calendar.application.command.*;
import com.wanted.momocity.calendar.application.usecase.CalendarCommandUseCase;
import com.wanted.momocity.calendar.domain.exception.CalendarAccessDeniedException;
import com.wanted.momocity.calendar.domain.exception.CalendarNotFoundException;
import com.wanted.momocity.calendar.domain.model.Calendar;
import com.wanted.momocity.calendar.domain.repository.CalendarRepository;
import com.wanted.momocity.calendar.presentation.api.response.MemoResponse;
import com.wanted.momocity.calendar.presentation.api.response.TodoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/*
 * comment.
 *  - 트랜잭션 경계 안에서 도메인 규칙 검증 + 저장 조율
 *  - 규칙 구현은 Domain 에 두고, Service 는 실행 순서에 집중
 *  - HTTP 모름, JPA 모름, 순수 비즈니스 흐름만 담당
 *  -
 *  [담당 UseCase]
 *  - CalendarCommandUseCase : Todo/Memo 쓰기 전부
 */

@Service
@Slf4j
@Transactional
public class CalendarCommandService implements CalendarCommandUseCase {

    private final CalendarRepository calendarRepository;
    private final StringRedisTemplate stringRedisTemplate;

    public CalendarCommandService(
            CalendarRepository calendarRepository,
            StringRedisTemplate stringRedisTemplate
    ) {
        this.calendarRepository = calendarRepository;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    // Todo
    @Override
    public TodoResponse handle(CreateTodoCommand command) {

        // 도메인 메서드로 Todo 생성
        Calendar calendar = Calendar.createTodo(
                command.userId(),
                command.title(),
                command.start()
        );

        // 저장
        Calendar saved = calendarRepository.save(calendar);
        evictCalendarCache(command.userId(), command.start());

        log.info("[Calendar] Todo 생성 완료 | userId={}, calendarId={}",
                command.userId(), saved.getId());

        return new TodoResponse(
                saved.getId(), saved.getTitle(),
                saved.getCategory(), saved.getStart(), saved.isCompleted()
        );
    }

    @Override
    public TodoResponse handle(UpdateTodoCommand command) {

        // 조회
        Calendar calendar = calendarRepository.findById(command.calendarId())
                .orElseThrow(() -> new CalendarNotFoundException("Todo 를 찾을 수 없습니다."));

        // 본인 소유 여부 확인 (도메인 메서드)
        if (!calendar.isOwnedBy(command.userId())) {
            throw new CalendarAccessDeniedException("본인의 Todo 만 수정할 수 있습니다.");
        }

        // 수정 (도메인 메서드)
        calendar.update(command.title(), command.start(), null);

        // 저장
        Calendar saved = calendarRepository.save(calendar);
        evictCalendarCache(command.userId(), command.start());

        log.info("[Calendar] Todo 수정 완료 | userId={}, calendarId={}",
                command.userId(), saved.getId());

        return new TodoResponse(
                saved.getId(), saved.getTitle(),
                saved.getCategory(), saved.getStart(), saved.isCompleted()
        );
    }

    @Override
    public void handle(DeleteTodoCommand command) {

        // 조회
        Calendar calendar = calendarRepository.findById(command.calendarId())
                .orElseThrow(() -> new CalendarNotFoundException("Todo 를 찾을 수 없습니다."));

        // 본인 소유 여부 확인 (도메인 메서드)
        if (!calendar.isOwnedBy(command.userId())) {
            throw new CalendarAccessDeniedException("본인의 Todo 만 삭제할 수 있습니다.");
        }

        // 삭제
        calendarRepository.delete(command.calendarId());
        evictCalendarCache(command.userId(), calendar.getStart());

        log.info("[Calendar] Todo 삭제 완료 | userId={}, calendarId={}",
                command.userId(), command.calendarId());
    }

    @Override
    public TodoResponse handle(CheckTodoCommand command) {

        // 조회
        Calendar calendar = calendarRepository.findById(command.calendarId())
                .orElseThrow(() -> new CalendarNotFoundException("Todo 를 찾을 수 없습니다."));

        // 본인 소유 여부 확인 (도메인 메서드)
        if (!calendar.isOwnedBy(command.userId())) {
            throw new CalendarAccessDeniedException("본인의 Todo 만 변경할 수 있습니다.");
        }

        // 체크 상태 변경 (Memo 호출 시 도메인에서 예외 발생)
        calendar.toggleComplete();

        // 저장
        Calendar saved = calendarRepository.save(calendar);
        evictCalendarCache(command.userId(), saved.getStart());

        log.info("[Calendar] Todo 체크 변경 완료 | userId={}, calendarId={}, isCompleted={}",
                command.userId(), saved.getId(), saved.isCompleted());

        return new TodoResponse(
                saved.getId(), saved.getTitle(),
                saved.getCategory(), saved.getStart(), saved.isCompleted()
        );
    }

    // Memo
    @Override
    public MemoResponse handle(CreateMemoCommand command) {

        // 도메인 메서드로 Memo 생성
        // end < start 검증은 도메인에서 처리
        Calendar calendar = Calendar.createMemo(
                command.userId(),
                command.title(),
                command.start(),
                command.end()
        );

        // 저장
        Calendar saved = calendarRepository.save(calendar);
        evictCalendarCache(command.userId(), command.start());

        log.info("[Calendar] Memo 생성 완료 | userId={}, calendarId={}",
                command.userId(), saved.getId());

        return new MemoResponse(
                saved.getId(), saved.getTitle(),
                saved.getCategory(), saved.getStart(), saved.getEnd()
        );
    }

    @Override
    public MemoResponse handle(UpdateMemoCommand command) {

        // 조회
        Calendar calendar = calendarRepository.findById(command.calendarId())
                .orElseThrow(() -> new CalendarNotFoundException("메모를 찾을 수 없습니다."));

        // 본인 소유 여부 확인 (도메인 메서드)
        if (!calendar.isOwnedBy(command.userId())) {
            throw new CalendarAccessDeniedException("본인의 메모만 수정할 수 있습니다.");
        }

        // 수정 (도메인 메서드)
        calendar.update(command.title(), command.start(), command.end());

        // 저장
        Calendar saved = calendarRepository.save(calendar);
        evictCalendarCache(command.userId(), command.start());

        log.info("[Calendar] Memo 수정 완료 | userId={}, calendarId={}",
                command.userId(), saved.getId());

        return new MemoResponse(
                saved.getId(), saved.getTitle(),
                saved.getCategory(), saved.getStart(), saved.getEnd()
        );
    }

    @Override
    public void handle(DeleteMemoCommand command) {

        // 조회
        Calendar calendar = calendarRepository.findById(command.calendarId())
                .orElseThrow(() -> new CalendarNotFoundException("메모를 찾을 수 없습니다."));

        // 본인 소유 여부 확인 (도메인 메서드)
        if (!calendar.isOwnedBy(command.userId())) {
            throw new CalendarAccessDeniedException("본인의 메모만 삭제할 수 있습니다.");
        }

        // 삭제
        calendarRepository.delete(command.calendarId());
        evictCalendarCache(command.userId(), calendar.getStart());

        log.info("[Calendar] Memo 삭제 완료 | userId={}, calendarId={}",
                command.userId(), command.calendarId());
    }
    
    /*
     * comment.
     *  캘린더 월별 캐시 무효화
     *  -
     *  캐시 키 : "calendar::{userId}:{year}:{month}" 삭제
     *  -> Todo/Memo 작성/수정/삭제/체크 변경 시 호출
     *  -
     *  예외처리
     *  - 캐시 무효화 실패 시 -> 로그만 남기고 서비스 계속 진행
     * */

    private void evictCalendarCache(Long userId, LocalDate date) {
        try {
            String cacheKey = "calendar::" + userId + ":" + date.getYear() + ":" + date.getMonthValue();
            stringRedisTemplate.delete(cacheKey);
            log.debug("[Calendar] 월별 캐시 무효화 완료 | key={}", cacheKey);
        } catch (Exception e) {
            log.warn("[Calendar] 월별 캐시 무효화 실패 | 예외={}", e.getMessage());
        }

    }

}
