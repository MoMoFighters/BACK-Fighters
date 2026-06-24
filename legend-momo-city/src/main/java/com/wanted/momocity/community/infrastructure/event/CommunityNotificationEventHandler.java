package com.wanted.momocity.community.infrastructure.event;

import com.wanted.momocity.community.domain.event.CommentCreatedEvent;
import com.wanted.momocity.community.domain.event.PostLikedEvent;
import com.wanted.momocity.community.domain.event.ReplyCreatedEvent;
import com.wanted.momocity.notification.application.service.NotificationHandlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/*
* comment.
*  Community 도메인 이벤트 수신 -> notification 저장
*  -
*  - @Async("domainEventExecutor") : 이벤트 처리를 별도의 스레드에서 실행, 응답 속도에 영향 없음
*  - TransactionPhase.AFTER_COMMIT : 트랜잭션 커멧 후에만 실행, 저장 실패해도 이벤트 처리 안 됨
* */

@Slf4j
@Component
@RequiredArgsConstructor
public class CommunityNotificationEventHandler {

    private final NotificationHandlerService notificationHandlerService;

    // 좋아요 알림
    @Async("domainEventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePostLiked(PostLikedEvent event) {
        log.info("[CommunityNotificationEventHandler] 좋아요 이벤트 수신 | postId={}", event.postId());
        notificationHandlerService.createPostLikedNotification(
                event.postOwnerId(), event.likedUserName(), event.postId());
    }

    // 댓글 알림
    @Async("domainEventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommentCreated(CommentCreatedEvent event) {
        log.info("[CommunityNotificationEventHandler] 댓글 이벤트 수신 | postId={}", event.postId());
        notificationHandlerService.createCommentNotification(
                event.postOwnerId(), event.commentUserName(), event.postId());
    }

    // 대댓글 알림
    @Async("domainEventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReplyCreated(ReplyCreatedEvent event) {
        log.info("[CommunityNotificationEventHandler] 대댓글 이벤트 수신 | postId={}", event.postId());
        notificationHandlerService.createReplyNotification(
                event.parentCommentOwnerId(),
                event.postOwnerId(),
                event.replyUserName(),
                event.postId());
    }

}
