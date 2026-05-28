package com.wanted.momocity.viewing.infrastructure.event;

import com.wanted.momocity.viewing.domain.event.ChapterCompletedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/*
 * ViewingEventHandler
 *
 * [역할]
 * ChapterCompletedEvent 후속 처리 담당
 * → 비즈니스 로직 없는 부가 작업 처리
 *
 * [@Async 이유]
 * → 이벤트 처리를 별도 스레드에서 실행
 * → saveProgress() 응답 속도에 영향 없음
 * → domainEventExecutor 스레드 풀 사용
 *
 * [@TransactionalEventListener AFTER_COMMIT 이유]
 * → 트랜잭션 커밋 전에 처리하면
 *   DB 저장 실패해도 이벤트 처리될 수 있음
 * → AFTER_COMMIT 으로 저장 성공 후에만 실행 보장
 */

@Slf4j
@Component
public class ViewingEventHandler {

    @Async("domainEventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ChapterCompletedEvent event) {
        log.info("[Viewing] ChapterCompletedEvent 처리 | userId={}, lectureId={}, chapterId={}, occurredAt={}",
                event.userId(), event.lectureId(), event.chapterId(), event.occurredAt());

        // TODO 모듈 04: 잔디(스트릭) 갱신
        // TODO 모듈 04: 수강 완료 처리 (totalProgress = 100 시)
        // TODO 알림 발송
    }

}
