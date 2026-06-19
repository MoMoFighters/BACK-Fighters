package com.wanted.momocity.streak.infrastructure.event;

import com.wanted.momocity.streak.application.usecase.StreakCommandUseCase;
import com.wanted.momocity.viewing.domain.event.ChapterCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDate;
import java.time.ZoneId;

/*
* comment.
*  ChapterCompletedEvent 구독 -> 잔디 누적 처리
*  -
*  [@Async]
*  - 이벤트 처리를 별도 스레드에서 실행
*  - saveProgress() 응답 속도에 영향 없음
*  - domainEventExecutor 스레드 풀 사용
*  -
*  [@TransactionalEventListener AFTER_COMMIT]
*  - 트랜잭션 커밋 전에 처리하면 DB 저장 실패해도 이벤트 처리될 수 있음
*  - AFTER_COMMIT 저장 성공 후에만 실행 보장
* */

@Slf4j
@Component
@RequiredArgsConstructor
public class StreakEventHandler {

    private final StreakCommandUseCase streakCommandUseCase;

    @Async("domainEventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ChapterCompletedEvent event) {

        log.info("[Streak] ChapterCompletedEvent 수신 | userId={}, chapterId={}",
                event.userId(), event.chapterId());

        // occurredAt (Instant) -> LocalDate 변환
        // -> 시스템 기본 시간대 기준으로 날짜 변환
        LocalDate streakDate = event.occurredAt()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        // 잔디 누적
        // 챕터 완료 시 해당 챕터의 durationSec 을 daily_watched_seconds 에 누적
        // -> StreakCommandUseCase.accumulate() 호출
        streakCommandUseCase.accumulate(
                event.userId(),
                streakDate,
                event.watchedSeconds()

        );

    }

}