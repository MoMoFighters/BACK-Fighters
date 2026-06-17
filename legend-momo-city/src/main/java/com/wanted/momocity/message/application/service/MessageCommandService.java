package com.wanted.momocity.message.application.service;

import com.wanted.momocity.friend.fmexception.FMBusinessRuleViolationException;
import com.wanted.momocity.friend.fmexception.FMResourceAccessDeniedException;
import com.wanted.momocity.friend.fmexception.FMResourceNotFoundException;
import com.wanted.momocity.friend.infrastructure.persistence.FriendJpaEntity;
import com.wanted.momocity.friend.user.UserWithFMJpaEntity;
import com.wanted.momocity.message.application.manager.ChatRoomSessionManager;
import com.wanted.momocity.message.application.policy.MessageEligibilityPolicy;
import com.wanted.momocity.message.application.usecase.MessageCommandUseCase;
import com.wanted.momocity.message.domain.event.LeftRoomPublishedEvent;
import com.wanted.momocity.message.domain.event.SendMessagePublishedEvent;
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
public class MessageCommandService implements MessageCommandUseCase {

    private final MessageSideUserRepository messageSideUserRepository;
    private final MessageSideFriendRepository messageSideFriendRepository;
    private final MessageRepository messageRepository;
    private final MessageEligibilityPolicy messageEligibilityPolicy;
    private final SpringDataMessageRepository springDataMessageRepository;
    private final SpringDataChatRoomMemberRepository springDataChatRoomMemberRepository;
    //notification에 행 추가
    private final ApplicationEventPublisher eventPublisher;
    //웹소켓
    private final ChatRoomSessionManager sessionManager;
    //웹소켓 브로드캐스팅 템플릿 주입
    private final SimpMessagingTemplate messagingTemplate;
    private final SpringDataChatRoomMemberRepository chatRoomMemberRepository;
    private final SpringDataChatRoomRepository springDataChatRoomRepository;

    private final SpringDataMessageReadRepository springDataMessageReadRepository;

    //채팅방 조회 및 개설
    @Override
    public CreateRoomView createChatRoomCommandHandle(Long userId, Long targetUserId) {
        log.info("[CreateChatRoomCommandService] 채팅방 조회 및 개설 비즈니스 시작 - 요청자: {}, 대상자: {}", userId, targetUserId);

        //404(사용자 없음)
        UserWithFMJpaEntity targetUser = messageSideUserRepository.findById(targetUserId)
                .orElseThrow(() -> new FMResourceNotFoundException("존재하지 않는 사용자와의 대화창을 개설할 수 없습니다."));

        //두 사람 사이의 관계 양방향 조회
        String friendStatus = "none";
        Optional<FriendJpaEntity> relationOpt = messageSideFriendRepository.findByFromUserId_IdAndToUserId_Id(userId, targetUserId);
        if (relationOpt.isEmpty()) {
            relationOpt = messageSideFriendRepository.findByFromUserId_IdAndToUserId_Id(targetUserId, userId);
        }
        if (relationOpt.isPresent()) {
            friendStatus = relationOpt.get().getStatus();
        }

        //나와의 채팅 차단, 친구 상태 검증 위임(409)
        messageEligibilityPolicy.validateCreate(userId, targetUserId,friendStatus);

        Long finalRoomId = null;

        //1차 검증: 두 유저가 채팅방 멤버에 같이 있는 채팅방이 있는지 조회
        //어댑터 포트를 통해 두 유저가 있는 기존 채팅방이 존재하는지 검증
        Optional<Long> existingRoomIdOpt = messageRepository.findExistingRoom(userId, targetUserId);
        if (existingRoomIdOpt.isPresent()) {
            log.info("[CreateChatRoomCommandService] 1차 멤버 검증 성공 - 양방향 활성화된 채팅방 발견. 기존 방ID: {}", existingRoomIdOpt.get());
            finalRoomId = existingRoomIdOpt.get();
        } else {
            //2차 검증: 로그인한 사용자가 나갔을 때 혼자 남은 방 중 과거 대화 역추적
            log.info("[CreateChatRoomCommandService] 1차 검증 실패(나간 유저 존재) -> 2차 메시지 교차 검증 역추적 시작...");

            //상대방이 참여 중인 모든 멤버 다져옴
            List<ChatRoomMemberJpaEntity> targetMemberships = springDataChatRoomMemberRepository.findByUserId_Id(targetUserId);

            for (ChatRoomMemberJpaEntity membership : targetMemberships) {
                //내가 나간 방일 가능성 있는 후보방
                Long candidateRoomId = membership.getRoomId().getId();

                //상대방이 참여 중인 그 방의 인원이 혼자인지 확인
                List<ChatRoomMemberJpaEntity> roomMembers = springDataChatRoomMemberRepository.findByRoomId_Id(candidateRoomId);

                if (roomMembers.size() == 1) {
                    //상대방이 혼자 남은 방에 로그인 유저가 보낸 메시지가 1개라도 존재하는 지 확인
                    //로그인 유저가 보낸 메시지가 있다면 로그인 유저가 나간 방
                    boolean hasMyPastMessage = springDataMessageRepository.existsByRoomId_IdAndSenderId_Id(candidateRoomId, userId);

                    if (hasMyPastMessage) {
                        finalRoomId = candidateRoomId;
                        log.info("[CreatChatRoomCommandService] 2차 교차 검증 성공 - 로그인 유저가 나갔던 과거 채팅방 발견: {}", finalRoomId);

                        //로그인 유저가 나갔던 방이므로 해당 채팅방 멤버로 복구
                        UserWithFMJpaEntity loginUser = messageSideUserRepository.getReferenceById(userId);
                        ChatRoomJpaEntity existingRoom = messageRepository.findChatRoomById(finalRoomId)
                                .orElseThrow(() -> new FMBusinessRuleViolationException("존재하지 않는 채팅방입니다."));

                        ChatRoomMemberJpaEntity myNewMembership = ChatRoomMemberJpaEntity.createMembership(existingRoom, loginUser);
                        messageRepository.saveChatRoomMember(myNewMembership);
                        log.info("[CreateChatRoomCommandService] 나갔던 로그인 유저(ID: {})를 기존 방(ID: {})의 멤버로 복구 완료", userId, finalRoomId);

                        break;
                    }
                }
            }
        }

        //기존 방 찾았다면 리턴
        if (finalRoomId != null) {
            return new CreateRoomView(
                    true,
                    finalRoomId,
                    targetUser.getId(),
                    targetUser.getNickname(),
                    targetUser.getRole(),
                    "FRIEND"
            );
        }

        //기존 채팅방 없으면 신규 개설
        ChatRoomJpaEntity newRoom = new ChatRoomJpaEntity();
        messageRepository.saveChatRoom(newRoom);

        log.info("[CreateChatRoomCommandService] 신규 채팅방 멤버 저장 시작 - 방ID: {}, 요청자: {}, 대상자: {}",
                newRoom.getId(), userId, targetUserId);

        //로그인 유저 jpaEntity로 담기
        UserWithFMJpaEntity loginUser = messageSideUserRepository.getReferenceById(userId);

        //로그인 유저 멤버 저장
        ChatRoomMemberJpaEntity myMembership = ChatRoomMemberJpaEntity.createMembership(newRoom, loginUser);
        messageRepository.saveChatRoomMember(myMembership);
        //상대방 멤버 저장
        ChatRoomMemberJpaEntity targetMembership = ChatRoomMemberJpaEntity.createMembership(newRoom, targetUser);
        messageRepository.saveChatRoomMember(targetMembership);

        log.info("[CreateChatRoomCommandService] 신규 채팅방 개설 완료 - 방ID: {}", newRoom.getId());

        return new CreateRoomView(
                false,
                newRoom.getId(),
                targetUser.getId(),
                targetUser.getNickname(),
                targetUser.getRole(),
                "FRIEND"
        );
    }

    //메시지 전송
    @Override
    public SendView sendMessageCommandHandle(Long senderId, Long roomId, String content) {
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
        MessageCommandService.WebSocketMessageDto wsPayload = new MessageCommandService.WebSocketMessageDto(
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

    //메시지 읽음
    @Override
    public ReadView readMessageCommandHandle(Long roomId, Long userId) {

        //방 존재 검증
        boolean existsRoom = springDataChatRoomRepository.existsById(roomId);
        if (!existsRoom) {
            throw new FMResourceNotFoundException("존재하지 않거나 삭제된 채팅방입니다.");
        }

        //권한 체크(방 멤버가 맞는지)
        boolean isMember = chatRoomMemberRepository.existsByRoomId_IdAndUserId_Id(roomId, userId);
        if (!isMember) {
            throw new FMResourceAccessDeniedException("해당 채팅방에 접근할 권한이 없습니다.");
        }

        //상대방 닉네임 추출
        List<ChatRoomMemberJpaEntity> members = chatRoomMemberRepository.findByRoomId_Id(roomId);
        UserWithFMJpaEntity targetUser = members.stream()
                .map(ChatRoomMemberJpaEntity::getUserId)
                .filter(user -> !user.getId().equals(userId))
                .findFirst()
                .orElse(members.get(0).getUserId()); //나와의 채팅방 대비

        //이 채팅방에서 상대방이 보낸 메시지 중 안읽은 메시지 뽑기
        //v2->읽음 테이블에서 userId가 로그인 유저이면서 메시지 안읽음 여부 확인(메시지 안읽고 알림만 읽은 경우 고려)
        List<MessageReadJpaEntity> unreadMessages = springDataMessageReadRepository.findByRoomId_IdAndUserId_IdAndIsMsgReadFalse(roomId, userId);

        //안읽은 메시지 리스트가 비어있는지 체크
        boolean hasUnread = !unreadMessages.isEmpty();

        //반복문 돌면서 상태를 true로 변경
        if (hasUnread) {
            for (MessageReadJpaEntity message : unreadMessages) {
                message.changeIsMsgRead(true); //메시지 읽음 처리
                message.changeIsNotiRead(true); //알림 읽음 처리
            } log.info("[ReadMessageCommandService] 상대방 메시지 {}건의 메시지 및 알림 읽음 통합 처리 완료", unreadMessages.size());
        } else {
            log.info("[ReadMessageCommandService] 읽을 메시지가 존재하지 않아 기존 상태 유지");
        }

        return new ReadView(
                roomId,
                targetUser.getId(),
                targetUser.getNickname(),
                hasUnread
        );
    }

    //채팅방 나가기
    @Override
    public LeaveChatRoomView leaveChatRoomCommandHandle(Long roomId, Long userId) {
        log.info("[LeaveChatRoomCommandService] 채팅방 나가기 검증 시작 - 방ID: {}, 유저ID: {}", roomId, userId);

        //나가려는 채팅방이 존재하는 지 검증(404)
        ChatRoomJpaEntity chatRoom = springDataChatRoomRepository.findById(roomId)
                .orElseThrow(() -> new FMResourceNotFoundException("존재하지 않거나 이미 나간 채팅방입니다."));

        //해당 방에 들어있는 모든 멤버 가져오기
        List<ChatRoomMemberJpaEntity> allMembers = springDataChatRoomMemberRepository.findByRoomId_Id(roomId);

        //정책 위임(403, 409)
        messageEligibilityPolicy.leaveChatRoom(userId, roomId, allMembers);

        //로그인 유저 정보 추출
        ChatRoomMemberJpaEntity myMembership = null;
        for (ChatRoomMemberJpaEntity member: allMembers) {
            if (member.getUserId().getId().equals(userId)) {
                myMembership = member;
                break;
            }
        }

        //방 분기
        int currentMemberCount = allMembers.size();

        //로그인 유저가 방에 남은 마지막 사용자일 때(chat_room, chat_room_member, message 모두 삭제)
        if (currentMemberCount <= 1) {
            log.info("[LeaveChatRoomCommandService] 마지막 사용자 퇴장 처리 -> 방 폭파 진행");
            springDataMessageRepository.deleteByRoomId_Id(roomId);
            springDataChatRoomMemberRepository.delete(myMembership);
            springDataChatRoomRepository.delete(chatRoom);

            return new LeaveChatRoomView(
                    true,
                    roomId,
                    null,
                    null,
                    null,
                    null
            );
        }

        //상대방이 남아있을 때(chat_room_member에서만 삭제)
        log.info("[LeaveChatRoomCommandService] 상대방 존재 확인 -> 로그인 유저 멤버 행만 삭제");
        springDataChatRoomMemberRepository.delete(myMembership);

        // 🎯 [수정]: 전체 멤버 중 '내가 아닌 사람(남겨진 사람)'을 정확히 찾아옵니다.
        // 이러면 나중에 다대다로 확장되어도 최소한 남은 사람 중 한 명을 안전하게 찝어올 수 있습니다.
        UserWithFMJpaEntity targetUser = null;
        for (ChatRoomMemberJpaEntity member : allMembers) {
            if (!member.getUserId().getId().equals(userId)) {
                targetUser = member.getUserId();
                break;
            }
        }

        String friendStatus = "none";
        Optional<FriendJpaEntity> relationOpt = messageSideFriendRepository.findByFromUserId_IdAndToUserId_Id(userId, targetUser.getId());
        if (relationOpt.isEmpty()) {
            relationOpt = messageSideFriendRepository.findByFromUserId_IdAndToUserId_Id(targetUser.getId(), userId);
        }
        if (relationOpt.isPresent()) {
            friendStatus = relationOpt.get().getStatus();
        }

        UserWithFMJpaEntity loginUser = myMembership.getUserId();

        eventPublisher.publishEvent(new LeftRoomPublishedEvent(
                roomId,
                userId,
                loginUser.getNickname()
        ));

        return new LeaveChatRoomView(
                false,
                roomId,
                targetUser.getId(),
                targetUser.getNickname(),
                targetUser.getRole(),
                friendStatus
        );
    }
}
