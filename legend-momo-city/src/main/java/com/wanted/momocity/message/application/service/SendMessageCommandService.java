package com.wanted.momocity.message.application.service;

import com.wanted.momocity.friend.fmexception.FMResourceNotFoundException;
import com.wanted.momocity.friend.infrastructure.persistence.FriendJpaEntity;
import com.wanted.momocity.friend.user.UserWithFMJpaEntity;
import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.momocity.message.application.manager.ChatRoomSessionManager;
import com.wanted.momocity.message.application.policy.MessageEligibilityPolicy;
import com.wanted.momocity.message.application.usecase.SendMessageCommandUseCase;
import com.wanted.momocity.message.domain.event.SendMessagePublishedEvent;
import com.wanted.momocity.message.domain.model.Message;
import com.wanted.momocity.message.domain.repository.ChatRoomQueryProjection;
import com.wanted.momocity.message.domain.repository.MessageRepository;
import com.wanted.momocity.message.infrastructure.persistence.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class SendMessageCommandService implements SendMessageCommandUseCase {

    private final MessageRepository messageRepository;
    private final MessageSideFriendRepository messageSideFriendRepository; //친구 상태 확인용
    private final MessageEligibilityPolicy messageEligibilityPolicy;
    private final MessageSideUserRepository messageSideUserRepository;
    //notification에 행 추가
    private final ApplicationEventPublisher eventPublisher;
    //웹소켓
    private final ChatRoomSessionManager sessionManager;
    //웹소켓 브로드캐스팅 템플릿 주입
    private final SimpMessagingTemplate messagingTemplate;

    //메시지 전송
    @Override
    public SendView handle(Long senderId, Long roomId, String content) {
        log.info("[SendMessageService] 메시지 전송 프로세스 시작 - 요청자: {}, 방번호: {}", senderId, roomId);

        UserWithFMJpaEntity sender = messageSideUserRepository.findUserById(senderId)
                .map(obj -> (UserWithFMJpaEntity) obj)
                .orElseThrow(() -> new FMResourceNotFoundException("존재하지 않는 사용자입니다."));

        //채팅방 조회
        ChatRoomJpaEntity chatRoom = messageRepository.findChatRoomById(roomId)
                .orElseThrow(() -> new FMResourceNotFoundException("존재하지 않는 채팅방입니다."));

        //어댑터에서 멤버 테이블 찔러서 현재 방에 소속된 멤버 긁어오기
        List<ChatRoomMemberJpaEntity> members = messageRepository.findMembersByRoomId(roomId);
        long roomMemberCount = members.size();

        //상대방 유저 추출
        UserWithFMJpaEntity targetUser = members.stream()
                .map(ChatRoomMemberJpaEntity::getUserId)
                .filter(user -> !user.getId().equals(senderId))
                .findFirst()
                .orElse(sender); //나와의 채팅방일 때 상대방이 없으므로 null

        //상대방과의 친구 관게 조회
        //두 사람 사이의 관계 양방향 조회
        String friendStatus = "none";
        if (targetUser == null) {
            friendStatus = "me";
        } else {
            Optional<FriendJpaEntity> relationOpt = messageSideFriendRepository.findByFromUserId_IdAndToUserId_Id(senderId, targetUser.getId());
            if (relationOpt.isEmpty()) {
                relationOpt = messageSideFriendRepository.findByFromUserId_IdAndToUserId_Id(targetUser.getId(), senderId);
            }
            if (relationOpt.isPresent()) {
                friendStatus = relationOpt.get().getStatus();
            }
        }

        messageEligibilityPolicy.sendable(roomId, senderId, friendStatus, roomMemberCount);

        //읽음 상태 기본 false
        boolean isRead = false;
        //웹소켓으로 상대방이 방에 머무는 거 확인 후 있다면 true 처리
        if (targetUser != null) {
            isRead = sessionManager.isUserInRoom(targetUser.getId(), roomId);
        }

        MessageJpaEntity newMessage = MessageJpaEntity.createNewMessage(chatRoom, sender, content, isRead);
        messageRepository.saveMessage(newMessage);

        //실시간 웹소켓 전송(프론트엔트가 구독 중인 주소로 메시지 주머니 투척)
        WebSocketMessageDto wsPayload = new WebSocketMessageDto(
                newMessage.getId(),
                senderId,
                sender.getNickname(),
                sender.getRole(),
                content,
                newMessage.getCreatedAt(),
                isRead
        );

        //WebSocketConfig에서 설정한 prefix "/sub" 채널로 발송
        String destination = "/sub/chat/room/" + roomId;
        messagingTemplate.convertAndSend(destination, wsPayload);
        log.info("[웹소켓 발송] {} 경로로 실시간 메시지 브로드캐스팅 완료", destination);

        // 🎯 2. 나와의 채팅방이 아닐 때만 상대방(targetUser)에게 알림 이벤트 발행
            log.info("[SendMessageService] 메시지 전송 성공 - 알림 발행. 수신자: {}", targetUser.getId());
            eventPublisher.publishEvent(new SendMessagePublishedEvent(
                    roomId,
                    senderId,
                    sender.getNickname(),   // 발신자 닉네임 추출
                    targetUser.getId(),
                    newMessage.getCreatedAt()
            ));

        return new SendView(
                roomId,
                targetUser.getId(),
                targetUser.getNickname(),
                targetUser.getRole(),
                friendStatus,
                content,
                newMessage.getCreatedAt()
        );
    }

    // 🎯 웹소켓 전송용 가벼운 내부 레코드(DTO) 생성
    public record WebSocketMessageDto(
            Long messageId,
            Long senderId,
            String nickname,
            String role,
            String content,
            java.time.LocalDateTime createdAt,
            boolean isRead
    ) {}
}
