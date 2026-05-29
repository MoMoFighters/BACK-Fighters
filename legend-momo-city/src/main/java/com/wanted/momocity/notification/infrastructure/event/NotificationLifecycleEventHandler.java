package com.wanted.momocity.notification.infrastructure.event;

import com.wanted.momocity.friend.domain.event.AcceptRequestFriendPublishedEvent;
import com.wanted.momocity.friend.domain.event.CancelRequestFriendPublishedEvent;
import com.wanted.momocity.friend.domain.event.RequestFriendPublishedEvent;
import com.wanted.momocity.message.domain.event.SendMessagePublishedEvent;
import com.wanted.momocity.notification.application.service.NotificationHandlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationLifecycleEventHandler {

    //알림 서비스 하나만 주입
    private final NotificationHandlerService notificationHandlerService;

    //친구 요청 완료 후 발행된 이벤트 처리
    @Async("domainEventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRequestFriend(RequestFriendPublishedEvent event) {
        log.info("[NotificationLifeCycleEventHandler] 친구 요청 이벤트 수신 -> 서비스로 이동");
        //서비스로 던기지
        notificationHandlerService.createAndSaveFriendRequestNotification(
                event.toUserId(),
                event.fromUserNickname(),
                event.friendId() //ref_id 용도
        );
    }

    //친구 요청 철회 완료 후 발행된 이벤트 처리
    @Async("domainEventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCancelRequestFriend(CancelRequestFriendPublishedEvent event) {
        log.info("[NotificationLifecycleEventHandler] 친구 요청 철회 이벤트 수신 -> 알림 서비스로 이동");

        //주입받은 알림 서비스로 토스
        notificationHandlerService.deleteRequestFriendNotification(event.friendId());
    }

    //친구 요청 수락 완료 후 발행된 이벤트 처리 (요청한 사람에게 친구 완료 알림 저장)
    @Async("domainEventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAcceptRequestFriend(AcceptRequestFriendPublishedEvent event) {
        log.info("[NotificationLifecycleEventHandler] 친구 요청 수락 이벤트 수신 -> 알림 서비스로 이동");

        notificationHandlerService.createAndSaveFriendAcceptNotification(
                event.triggerUserId(), //알림을 일으킨 사람
                event.acceptorNickname(), //문구에 들어갈 수락자 닉네임
                event.friendId() //refId용
        );

    }

    //메시지 전송
    @Async("domainEventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSendMessage(SendMessagePublishedEvent event) {
        log.info("[NotificationLifecycleEventHandler] 메시지 전송 -> 알림 서비스로 이동");

        notificationHandlerService.sendMessageNotification(
                event.roomId(), //refId용
                event.senderNickname(), //문구에 들어갈 보낸 사람 닉네임
                event.senderId(), //보낸 사람
                event.receiverId(),
                event.createdAt() //날짜 업데이트용
        );

    }
}
