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
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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
@RequiredArgsConstructor
public class CalendarCommandService implements CalendarCommandUseCase {

    private final CalendarRepository calendarRepository;
    private final CacheManager redisCacheManager;

    // Todo
    @Override
    @CacheEvict(value = "calendar", key = "#command.userId() + ':' + #command.start().year + ':' + #command.start().monthValue", cacheManager = "redisCacheManager")
    public TodoResponse handle(CreateTodoCommand command) {

        // 도메인 메서드로 Todo 생성
        Calendar calendar = Calendar.createTodo(
                command.userId(),
                command.title(),
                command.start()
        );

        // 저장
        Calendar saved = calendarRepository.save(calendar);

        log.info("[Calendar] Todo 생성 완료 | userId={}, calendarId={}",
                command.userId(), saved.getId());

        return new TodoResponse(
                saved.getId(), saved.getTitle(),
                saved.getCategory(), saved.getStart(), saved.isCompleted()
        );
    }

    @Override
    @CacheEvict(value = "calendar", key = "#command.userId() + ':' + #command.start().year + ':' + #command.start().monthValue", cacheManager = "redisCacheManager")
    public TodoResponse handle(UpdateTodoCommand command) {

        // 조회
        Calendar calendar = calendarRepository.findById(command.calendarId())
                .orElseThrow(() -> new CalendarNotFoundException("Todo 를 찾을 수 없습니다."));

        // 본인 소유 여부 확인 (도메인 메서드)
        if (!calendar.isOwnedBy(command.userId())) {
            throw new CalendarAccessDeniedException("본인의 Todo 만 수정할 수 있습니다.");
        }

        // 날짜(달)가 바뀌는 케이스 방어 -> 수정 전 start 를 먼저 기억해둠
        // (@CacheEvict 로는 "새 start" 캐시만 지워져서, 옛 달로 옮겨진 Todo가 옛 달 캐시에 유령으로 남는 문제가 있었음)
        LocalDate oldStart = calendar.getStart();

        // 수정 (도메인 메서드)
        calendar.update(command.title(), command.start(), null);

        // 저장
        Calendar saved = calendarRepository.save(calendar);

        // 옛 달 + 새 달 캐시 둘 다 커밋 이후 evict (Todo는 end 없으므로 null 전달)
        evictCalendarCacheAfterCommit(command.userId(), oldStart, null);
        evictCalendarCacheAfterCommit(command.userId(), saved.getStart(), null);

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

        // 즉시 evict 대신, 커밋 이후로 미루는 헬퍼 호출로 교체
        // Todo는 end가 없으므로 null 전달
        evictCalendarCacheAfterCommit(command.userId(), calendar.getStart(), null);

        calendarRepository.delete(command.calendarId());

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

        // CheckTodoCommand 에는 start 필드가 없어 #command.start() SpEL이 런타임 예외를 던졌음
        // -> 조회해둔 calendar 의 start 로 수동 evict
        evictCalendarCacheAfterCommit(command.userId(), calendar.getStart(), null);

        log.info("[Calendar] Todo 체크 변경 완료 | userId={}, calendarId={}, isCompleted={}",
                command.userId(), saved.getId(), saved.isCompleted());

        return new TodoResponse(
                saved.getId(), saved.getTitle(),
                saved.getCategory(), saved.getStart(), saved.isCompleted()
        );
    }

    // Memo
    @Override
    @CacheEvict(value = "calendar", key = "#command.userId() + ':' + #command.start().year + ':' + #command.start().monthValue", cacheManager = "redisCacheManager")
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

        log.info("[Calendar] Memo 생성 완료 | userId={}, calendarId={}",
                command.userId(), saved.getId());

        return new MemoResponse(
                saved.getId(), saved.getTitle(),
                saved.getCategory(), saved.getStart(), saved.getEnd()
        );
    }

    @Override
    @CacheEvict(value = "calendar", key = "#command.userId() + ':' + #command.start().year + ':' + #command.start().monthValue", cacheManager = "redisCacheManager")
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

        // 즉시 evict 대신 커밋 이후로 미루고, end가 다른 달이면 그 쪽도 같이 evict
        evictCalendarCacheAfterCommit(command.userId(), calendar.getStart(), calendar.getEnd());

        calendarRepository.delete(command.calendarId());

        log.info("[Calendar] Memo 삭제 완료 | userId={}, calendarId={}",
                command.userId(), command.calendarId());
    }

    private void evictCalendarCacheAfterCommit(Long userId, LocalDate start, LocalDate end) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                evictCacheKey(userId, start.getYear(), start.getMonthValue());

                // end가 존재하고 start와 다른 년/월이면 그 쪽 캐시도 같이 evict
                // (Todo는 end가 없으므로 null로 넘어오면 이 블록은 스킵됨)
                if (end != null
                        && (end.getYear() != start.getYear() || end.getMonthValue() != start.getMonthValue())) {
                    evictCacheKey(userId, end.getYear(), end.getMonthValue());
                }
            }
        });
    }

    // 캐시 키 하나를 evict하는 저수준 헬퍼 (null 캐시 방어 포함)
    private void evictCacheKey(Long userId, int year, int month) {
        String cacheKey = userId + ":" + year + ":" + month;
        var cache = redisCacheManager.getCache("calendar");
        if (cache != null) {
            cache.evict(cacheKey);
        } else {
            log.warn("[Calendar] calendar 캐시를 찾을 수 없어 evict 스킵 | key={}", cacheKey);
        }
    }

}
