package com.wanted.momocity.message.infrastructure.event;

import com.wanted.momocity.message.application.query.FindChatRoomQuery;
import com.wanted.momocity.message.application.query.GetMessageHistoryQuery;
import com.wanted.momocity.message.application.usecase.MessageQueryUseCase;
import com.wanted.momocity.message.domain.event.*;
import com.wanted.momocity.notification.application.query.GetPhoneAppCountsQuery;
import com.wanted.momocity.notification.application.usecase.NotificationQueryUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
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
    private final NotificationQueryUseCase notificationQueryUseCase;
    // 🎯 [핵심] 현재 웹소켓 세션이 살아있는 유저인지 메모리에서 즉시 판별하기 위한 레지스트리 주입
    private final SimpUserRegistry simpUserRegistry;

    /**
     * 🎯 [성능 개선 핵심]
     * 원래 트랜잭션이 정상 커밋(AFTER_COMMIT)된 후, 메인 스레드와 분리된 별도 @Async 스레드 풀에서
     * 무거운 목록 무한 루프 및 히스토리 조회 쿼리를 비동기로 쏘아 올립니다.
     */
    //친구 삭제로 인한 채팅방 나가기
    @Async("domainEventExecutor") // 프로젝트 환경에 맞는 Async 스레드 풀 지정
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleLeaveChatRoomWebSocketRefresh(LeaveChatRoomWebsocketPublishedEvent event) {
        Long roomId = event.roomId();
        Long targetUserId = event.userId();

        // 🌟 [Short-Circuit] 상대방이 오프라인이면 무거운 DB 조회 쿼리 자체를 차단
        if (simpUserRegistry.getUser(targetUserId.toString()) == null) {
            log.debug("[LeaveChatRoom] 대상 유저(ID: {}) 오프라인 -> 무거운 조회 및 갱신 패스", targetUserId);
            return;
        }

        log.info("[LeaveChatRoomEventListener] 비동기 웹소켓 갱신 스레드 진입 - 방ID: {}, 대상자: {}", roomId, targetUserId);

        // 🌟 [추가] 시간 낭비 없이 웹소켓 버그만 해결하기!
        // 상대방(targetUserId) 화면에 실시간으로 반영되도록 템플릿으로 쏴버립니다.
        String destination = "/sub/chat/room/" + roomId;

        try {
            // 1) 상대방 채팅방 내부 메시지 내역 새로고침 데이터 발송
            List<MessageQueryUseCase.MessageHistoryView> historyPayload =
                    messageQueryUseCase.getMessageHistoryQueryHandle(new GetMessageHistoryQuery(roomId, targetUserId, null));
            messagingTemplate.convertAndSendToUser(targetUserId.toString(), destination, historyPayload);

            // 2) 상대방 전체 채팅방 리스트 화면 새로고침 데이터 발송
            List<MessageQueryUseCase.ChatRoomView> chatRoomListPayload =
                    messageQueryUseCase.getChatRoomQueryHandle(new FindChatRoomQuery(targetUserId));
            messagingTemplate.convertAndSendToUser(targetUserId.toString(), "/sub/chat/rooms", chatRoomListPayload);
            log.info("[MessageHandlerService] 친구 삭제로 채팅방 나가기 완료 및 상대방(ID:{})에게 웹소켓 전송 완료", targetUserId);
        } catch (Exception e) {
            log.error("[LeaveChatRoomEventListener] 단건 유저 갱신 실패 - 유저ID: {}, 사유: {}", targetUserId, e.getMessage(), e);
        }
    }

    @Async("domainEventExecutor") // 프로젝트 환경에 맞는 Async 스레드 풀 지정
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleChatRoomReenteredWebSocketRefresh(ChatRoomReenteredPublishedEvent event) {
        String destination = "/sub/chat/room/" + event.roomId();

        for (Long memberId : event.memberIds()) {
            // 🌟 [Short-Circuit] 오프라인 유저는 루프 내에서 조기 스킵하여 무거운 대화방/목록 조회 SQL 유실 차단
            if (simpUserRegistry.getUser(memberId.toString()) == null) {
                log.debug("[ChatRoomReentered] 유저(ID: {}) 오프라인 -> 루프 스킵", memberId);
                continue;
            }
            try {
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
            } catch (Exception e) {
                log.error("[ChatRoomReentered] 유저(ID: {}) 웹소켓 새로고침 처리 중 예외 발생 (건너뛰고 계속 진행)", memberId, e);
            }
        }
        log.info("[MessageHandlerService] 일대일 채팅방 재입장 완료로 웹소켓 전송 완료");
    }

    @Async("domainEventExecutor") // 프로젝트 환경에 맞는 Async 스레드 풀 지정
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleChatMessageSentWebSocketRefresh(ChatMessageSentWebsocketPublishedEvent event) {
        Long roomId = event.roomId();
        Long senderId = event.senderId();
        String destination = "/sub/chat/room/" + roomId;

        for (Long receiverId : event.receiverIds()) {
            // 🌟 [Short-Circuit] 수신자들 중 앱을 끈 오프라인 사용자들은 대량의 Fan-out SQL 대상에서 전면 제외
            if (simpUserRegistry.getUser(receiverId.toString()) == null) {
                log.debug("[ChatMessageSent] 수신자(ID: {}) 오프라인 -> 조회 루프 스킵", receiverId);
                continue;
            }
            try {
                // 💬 1) 각 수신자의 대화 히스토리 화면 리로드 데이터 조회 및 전송
                List<MessageQueryUseCase.MessageHistoryView> historyPayload =
                        messageQueryUseCase.getMessageHistoryQueryHandle(new GetMessageHistoryQuery(roomId, receiverId, null));
                messagingTemplate.convertAndSendToUser(receiverId.toString(), destination, historyPayload);

                // 🗂️ 2) 각 수신자의 전체 채팅방 리스트 화면 리로드 데이터 조회 및 전송
                List<MessageQueryUseCase.ChatRoomView> chatRoomListPayload =
                        messageQueryUseCase.getChatRoomQueryHandle(new FindChatRoomQuery(receiverId));
                messagingTemplate.convertAndSendToUser(receiverId.toString(), "/sub/chat/rooms", chatRoomListPayload);

                // 📱 3) 내가 보낸 게 아닐 때만, 수신자 휴대폰 앱 배지 카운트 실시간 전송
                if (!receiverId.equals(senderId)) {
                    notificationQueryUseCase.getPhoneAppCountsQueryHandle(new GetPhoneAppCountsQuery(receiverId));
                }
            } catch (Exception e) {
                log.error("[ChatMessageSent] 수신자(ID: {}) 웹소켓 및 알림 카운트 갱신 실패 (건너뛰고 계속 진행)", receiverId, e);
            }
        }
        log.info("[MessageWebsocketListener] 메시지 발송에 따른 전원 실시간 화면 및 앱 배지 갱신 완료");
    }

    @Async("domainEventExecutor") // 프로젝트 환경에 맞는 Async 스레드 풀 지정
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleChatRoomReadWebSocketRefresh(ChatRoomReadWebsocketPublishedEvent event) {
        Long roomId = event.roomId();
        Long readerId = event.readerId();
        String destination = "/sub/chat/room/" + roomId;

        // 1. 방 안의 모든 사람(나 포함)의 대화 내역 및 채팅방 목록 배지 차감 실시간 배달
        for (Long memberId : event.memberIds()) {
            // 🌟 [Short-Circuit] 안읽은 배지 차감용 실시간 화면 갱신도 온라인 유저에게만 작동하도록 격리
            if (simpUserRegistry.getUser(memberId.toString()) == null) {
                continue;
            }
            try {
                // 💬 대화 히스토리 화면 리로드 전송
                List<MessageQueryUseCase.MessageHistoryView> historyPayload = messageQueryUseCase.getMessageHistoryQueryHandle(new GetMessageHistoryQuery(roomId, memberId, null));
                messagingTemplate.convertAndSendToUser(memberId.toString(), destination, historyPayload);

                // 🗂️ 전체 채팅방 리스트 갱신 전송
                List<MessageQueryUseCase.ChatRoomView> chatRoomListPayload = messageQueryUseCase.getChatRoomQueryHandle(new FindChatRoomQuery(memberId));
                messagingTemplate.convertAndSendToUser(memberId.toString(), "/sub/chat/rooms", chatRoomListPayload);
            } catch (Exception e) {
                log.error("[ChatRoomRead] 멤버(ID: {}) 화면 데이터 웹소켓 전송 실패", memberId, e);
            }
        }

        try {
            // 📱 2. 읽은 사람 본인의 전체 앱 배지 실시간 차감 반영
            notificationQueryUseCase.getPhoneAppCountsQueryHandle(new GetPhoneAppCountsQuery(readerId));
        } catch (Exception e) {
            log.error("[ChatRoomRead] 읽은 유저 본인(ID: {}) 앱 배지 갱신 실패", readerId, e);
        }
        log.info("[MessageWebsocketListener] 방 진입/읽음에 따른 전원 화면 데이터 및 읽은이(ID: {})의 앱 배지 갱신 비동기 완료", readerId);
    }

    //채팅방 나가기
    @Async("domainEventExecutor") // 프로젝트 환경에 맞는 Async 스레드 풀 지정
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleChatRoomLeaveWebSocketRefresh(ChatRoomLeaveWebsocketPublishedEvent event) {
        Long roomId = event.roomId();
        Long readerId = event.leaveUserId();
        String destination = "/sub/chat/room/" + roomId;

        // 1. 방 안의 모든 사람(나 포함)의 대화 내역 및 채팅방 목록 배지 차감 실시간 배달
        for (Long memberId : event.receiverIds()) {
            // 🌟 [Short-Circuit] 퇴장 알림을 받을 남은 인원 중 오프라인 유저 스킵
            if (simpUserRegistry.getUser(memberId.toString()) == null) {
                continue;
            }
            try {
                // 💬 대화 히스토리 화면 리로드 전송
                List<MessageQueryUseCase.MessageHistoryView> historyPayload = messageQueryUseCase.getMessageHistoryQueryHandle(new GetMessageHistoryQuery(roomId, memberId, null));
                messagingTemplate.convertAndSendToUser(memberId.toString(), destination, historyPayload);

                // 🗂️ 전체 채팅방 리스트 갱신 전송
                List<MessageQueryUseCase.ChatRoomView> chatRoomListPayload = messageQueryUseCase.getChatRoomQueryHandle(new FindChatRoomQuery(memberId));
                messagingTemplate.convertAndSendToUser(memberId.toString(), "/sub/chat/rooms", chatRoomListPayload);
            } catch (Exception e) {
                log.error("[ChatRoomLeave] 남은 멤버(ID: {}) 웹소켓 전송 실패", memberId, e);
            }
        }

        try {
            // 📱 2. 읽은 사람 본인의 전체 앱 배지 실시간 차감 반영
            notificationQueryUseCase.getPhoneAppCountsQueryHandle(new GetPhoneAppCountsQuery(readerId));
        } catch (Exception e) {
            log.error("[ChatRoomLeave] 나간 유저(ID: {}) 앱 배지 최종 차감 실패", readerId, e);
        }
        log.info("[LeaveChatRoomEventListener] 남은 전체 인원({})에게 웹소켓 전송 완료", event.receiverIds());
    }

    // 채팅방 이름 변경에 따른 전원 화면 갱신
    @Async("domainEventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleChatRoomRenamedWebSocketRefresh(ChatRoomRenamedWebsocketPublishedEvent event) {
        Long roomId = event.roomId();
        String destination = "/sub/chat/room/" + roomId;

        log.info("[MessageWebsocketListener] 방 이름 변경 비동기 웹소켓 갱신 시작 - 방ID: {}, 대상 인원: {}명", roomId, event.memberIds().size());

        // 각 참여자들의 대화 히스토리 및 전체 채팅방 목록 화면을 독립된 쿼리로 가공하여 실시간 전송
        for (Long memberId : event.memberIds()) {
            // 🌟 [Short-Circuit] 방 이름 변경 갱신 시 오프라인 유저 스킵
            if (simpUserRegistry.getUser(memberId.toString()) == null) {
                continue;
            }
            try {
                // 💬 1) 대화 히스토리 화면 리로드 데이터 전송
                List<MessageQueryUseCase.MessageHistoryView> historyPayload =
                        messageQueryUseCase.getMessageHistoryQueryHandle(new GetMessageHistoryQuery(roomId, memberId, null));
                messagingTemplate.convertAndSendToUser(memberId.toString(), destination, historyPayload);

                // 🗂️ 2) 전체 채팅방 리스트 화면 리로드 데이터 전송
                List<MessageQueryUseCase.ChatRoomView> chatRoomListPayload =
                        messageQueryUseCase.getChatRoomQueryHandle(new FindChatRoomQuery(memberId));
                messagingTemplate.convertAndSendToUser(memberId.toString(), "/sub/chat/rooms", chatRoomListPayload);
            } catch (Exception e) {
                log.error("[ChatRoomRenamed] 멤버(ID: {}) 방 이름 변경 실시간 갱신 실패", memberId, e);
            }
        }

        log.info("[MessageWebsocketListener] 채팅방 이름 변경에 따른 전원 실시간 대화방/목록 갱신 완료");
    }

    // 멤버 초대로 인한 신규 유저 포함 전원 웹소켓 화면 갱신
    @Async("domainEventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleChatRoomMemberInvitedWebSocketRefresh(ChatRoomMemberInviteWebsocketPublishedEvent event) {
        Long roomId = event.roomId();
        String inviteDestination = "/sub/chat/room/" + roomId;

        log.info("[MessageWebsocketListener] 멤버 초대 비동기 웹소켓 갱신 시작 - 방ID: {}, 대상 인원: {}명", roomId, event.targetMemberIds().size());

        for (Long targetMemberId : event.targetMemberIds()) {
            // 🌟 [Short-Circuit] 초대된 인원 중 오프라인 유저 스킵
            if (simpUserRegistry.getUser(targetMemberId.toString()) == null) {
                continue;
            }
            try {
                // 💬 1) 대화 히스토리 화면 리로드 데이터 전송
                List<MessageQueryUseCase.MessageHistoryView> historyPayload =
                        messageQueryUseCase.getMessageHistoryQueryHandle(new GetMessageHistoryQuery(roomId, targetMemberId, null));
                messagingTemplate.convertAndSendToUser(targetMemberId.toString(), inviteDestination, historyPayload);

                // 🗂️ 2) 전체 채팅방 리스트 화면 리로드 데이터 전송
                List<MessageQueryUseCase.ChatRoomView> chatRoomListPayload =
                        messageQueryUseCase.getChatRoomQueryHandle(new FindChatRoomQuery(targetMemberId));
                messagingTemplate.convertAndSendToUser(targetMemberId.toString(), "/sub/chat/rooms", chatRoomListPayload);
            } catch (Exception e) {
                log.error("[ChatRoomMemberInvited] 초대 타겟 멤버(ID: {}) 웹소켓 화면 갱신 실패", targetMemberId, e);
            }
        }
        log.info("[MessageWebsocketListener] 멤버 초대로 인한 전원 실시간 대화방/목록 갱신 완료");
    }
}
