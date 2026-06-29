package com.wanted.momocity.streak.infrastructure.event;

import com.wanted.momocity.streak.application.usecase.StreakCommandUseCase;
import com.wanted.momocity.viewing.domain.event.ProgressSavedEvent;
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
    public void handle(ProgressSavedEvent event) {

        log.info("[Streak] ProgressSavedEvent 수신 | userId={}, chapterId={}",
                event.userId(), event.chapterId());

        // 잔디 누적
        // 미완료 챕터 : 실제 증분만큼 누적
        // 완료된 챕터 : playbackSeconds 만큼 누적
        streakCommandUseCase.accumulate(
                event.userId(),
                event.date(),
                event.watchedSeconds()
        );

    }

}