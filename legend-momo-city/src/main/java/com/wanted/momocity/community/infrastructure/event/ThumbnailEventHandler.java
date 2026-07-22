package com.wanted.momocity.community.infrastructure.event;

import com.wanted.momocity.community.application.post.service.ThumbnailAsyncService;
import com.wanted.momocity.community.domain.event.ThumbnailResizeRequestedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/*
 * comment.
 *  ThumbnailEventHandler
 *  - ThumbnailResizeRequestedEvent 수신
 *  - AFTER_COMMIT: 메인 트랜잭션(uploadContents/updateContents)이
 *    실제로 커밋된 이후에만 리스너가 실행됨
 *  - 여기서 비로소 ThumbnailAsyncService(@Async) 호출
 *    -> 커밋 전에 별도 스레드가 DB를 조회하는 레이스 컨디션 방지
 */

@Component
@RequiredArgsConstructor
public class ThumbnailEventHandler {

    private final ThumbnailAsyncService thumbnailAsyncService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ThumbnailResizeRequestedEvent event) {
        thumbnailAsyncService.resizeThumbnailAsync(event.postId(), event.originalThumbnailUrl());
    }

}
