package com.wanted.momocity.message.infrastructure.event;

import com.wanted.momocity.auth.domain.event.SignupCompletedEvent;
import com.wanted.momocity.friend.domain.event.DeleteFriendPublishedEvent;
import com.wanted.momocity.message.application.service.MessageHandlerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageLifecycleEventHandler {

    private final MessageHandlerService messageHandlerService;

    //회원가입 시 나와의 채팅방 생성
    @Async("domainEventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRegisterMyChatRoom(SignupCompletedEvent event) {
        log.info("[MessageLifecycleEventHandler] 회원가입 완료로 인한 나와의 채팅 최초 1회 생성");

        messageHandlerService.createSelfChatRoom(
                event.userId()
        );
    }

    //친구 삭제 시 채팅방 나가기
    @Async("domainEventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDeleteFriend(DeleteFriendPublishedEvent event) {
        log.info("[MessageLifecycleEventHandler] 친구 삭제 완료로 인한 채팅방 나가기 처리");

        messageHandlerService.leaveChatRoom(
                event.loginUserId(),
                event.targetUserId()
        );
    }
}
