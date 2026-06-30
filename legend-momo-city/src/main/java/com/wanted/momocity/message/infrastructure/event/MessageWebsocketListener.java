package com.wanted.momocity.message.infrastructure.event;

import com.wanted.momocity.message.application.query.FindChatRoomQuery;
import com.wanted.momocity.message.application.query.GetMessageHistoryQuery;
import com.wanted.momocity.message.application.usecase.MessageQueryUseCase;
import com.wanted.momocity.message.domain.event.ChatRoomReenteredPublishedEvent;
import com.wanted.momocity.message.domain.event.LeaveChatRoomWebsocketPublishedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageWebsocketListener {

    private final MessageQueryUseCase messageQueryUseCase;
    private final SimpMessagingTemplate messagingTemplate; // 실시간 웹소켓 발송을 위한 템플릿

    /**
     * 🎯 [성능 개선 핵심]
     * 원래 트랜잭션이 정상 커밋(AFTER_COMMIT)된 후, 메인 스레드와 분리된 별도 @Async 스레드 풀에서
     * 무거운 목록 무한 루프 및 히스토리 조회 쿼리를 비동기로 쏘아 올립니다.
     */
    @Async("domainEventExecutor") // 프로젝트 환경에 맞는 Async 스레드 풀 지정
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleLeaveChatRoomWebSocketRefresh(LeaveChatRoomWebsocketPublishedEvent event) {
        Long roomId = event.roomId();
        Long targetUserId = event.userId();

        log.info("[LeaveChatRoomEventListener] 비동기 웹소켓 갱신 스레드 진입 - 방ID: {}, 대상자: {}", roomId, targetUserId);

        // 🌟 [추가] 시간 낭비 없이 웹소켓 버그만 해결하기!
        // 상대방(targetUserId) 화면에 실시간으로 반영되도록 템플릿으로 쏴버립니다.
        String destination = "/sub/chat/room/" + roomId;

        // 1) 상대방 채팅방 내부 메시지 내역 새로고침 데이터 발송
        List<MessageQueryUseCase.MessageHistoryView> historyPayload =
                messageQueryUseCase.getMessageHistoryQueryHandle(new GetMessageHistoryQuery(roomId, targetUserId, null));
        messagingTemplate.convertAndSendToUser(targetUserId.toString(), destination, historyPayload);

        // 2) 상대방 전체 채팅방 리스트 화면 새로고침 데이터 발송
        List<MessageQueryUseCase.ChatRoomView> chatRoomListPayload =
                messageQueryUseCase.getChatRoomQueryHandle(new FindChatRoomQuery(targetUserId));
        messagingTemplate.convertAndSendToUser(targetUserId.toString(), "/sub/chat/rooms", chatRoomListPayload);
        log.info("[MessageHandlerService] 친구 삭제로 채팅방 나가기 완료 및 상대방(ID:{})에게 웹소켓 전송 완료", targetUserId);
    }

    @Async("domainEventExecutor") // 프로젝트 환경에 맞는 Async 스레드 풀 지정
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleChatRoomReenteredWebSocketRefresh(ChatRoomReenteredPublishedEvent event) {
        String destination = "/sub/chat/room/" + event.roomId();

        for (Long memberId : event.memberIds()) {
            // 💬 1) 각 참여자의 내부 대화 히스토리 화면 리로드 데이터 조회 및 전송
            List<MessageQueryUseCase.MessageHistoryView> historyPayload =
                    messageQueryUseCase.getMessageHistoryQueryHandle(
                            new GetMessageHistoryQuery(event.roomId(), memberId, null)
                    );
            messagingTemplate.convertAndSendToUser(memberId.toString(), destination, historyPayload);

            // 🗂️ 2) 각 참여자의 전체 채팅방 리스트 화면 리로드 데이터 조회 및 전송
            List<MessageQueryUseCase.ChatRoomView> chatRoomListPayload =
                    messageQueryUseCase.getChatRoomQueryHandle(
                            new FindChatRoomQuery(memberId)
                    );
            messagingTemplate.convertAndSendToUser(memberId.toString(), "/sub/chat/rooms", chatRoomListPayload);
        }
        log.info("[MessageHandlerService] 일대일 채팅방 재입장 완료로 웹소켓 전송 완료");
    }
}
