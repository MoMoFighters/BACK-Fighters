package com.wanted.momocity.notification.infrastructure.event;

import com.wanted.momocity.friend.domain.event.AcceptRequestFriendPublishedEvent;
import com.wanted.momocity.friend.domain.event.CancelRequestFriendPublishedEvent;
import com.wanted.momocity.friend.domain.event.RequestFriendPublishedEvent;
import com.wanted.momocity.friend.domain.event.TeacherStudentAutoFriendPublishedEvent;
import com.wanted.momocity.message.domain.event.SendMessagePublishedEvent;
import com.wanted.momocity.notification.application.service.NotificationHandlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
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
                event.toUserId(), //userId에 넣기(알림 받는 사람)
                event.fromUserNickname(),
                event.fromUserId() //ref_id 용도(알림 발생시킨 사람)
        );
    }

    //친구 요청 철회 완료 후 발행된 이벤트 처리
    @Async("domainEventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCancelRequestFriend(CancelRequestFriendPublishedEvent event) {
        log.info("[NotificationLifecycleEventHandler] 친구 요청 철회 이벤트 수신 -> 알림 서비스로 이동");

        //주입받은 알림 서비스로 토스
        notificationHandlerService.deleteRequestFriendNotification(
                event.fromUserId(), //refId 요청자
                event.toUserId() //userId 대상자
        );
    }

    //친구 요청 수락 완료 후 발행된 이벤트 처리 (요청한 사람에게 친구 완료 알림 저장)
    @Async("domainEventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAcceptRequestFriend(AcceptRequestFriendPublishedEvent event) {
        log.info("[NotificationLifecycleEventHandler] 친구 요청 수락 이벤트 수신 -> 알림 서비스로 이동");

        notificationHandlerService.createAndSaveFriendAcceptNotification(
                event.acceptorUserId(), //수락한 사람
                event.acceptorNickname(), //문구에 들어갈 수락자 닉네임
                event.fromUserId() //refId용
        );

    }

    //메시지 전송
    @Async("domainEventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSendMessage(SendMessagePublishedEvent event) {
        log.info("[NotificationLifecycleEventHandler] 메시지 전송 -> 알림 서비스로 이동");

        //다대다인 경우 수신자가 null
        Long receiverId = (event.receiverId() != null) ? event.receiverId().getId() : null;

        notificationHandlerService.sendMessageNotification(
                event.roomId(), //refId용
                event.roomTitle(), //다대다를 위한 채팅방 이름
                event.senderId(), //보낸 사람
                event.senderNickname(), //문구에 들어갈 보낸 사람 닉네임
                receiverId,
                event.createdAt() //날짜 업데이트용
        );
    }

    //강사-학생 자동 친구 학생 쪽 알림 행 추가
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAutoFriend(TeacherStudentAutoFriendPublishedEvent event) {
        log.info("[NotificationLifecycleEventHandler] 강사-학생 자동 친구 행 추가 이벤트 수신 -> 알림 서비스로 이동");

        notificationHandlerService.autoFriendNotification(
                event.fromUserId(), //학생 아이디(notification 테이블의 userId: 알림 받을 사람)
                event.toUserId(), //강사 아이디(refId)
                event.teacherName(), //강사 이름
                event.teacherNickname() //강사 닉네임
        );
    }
}
