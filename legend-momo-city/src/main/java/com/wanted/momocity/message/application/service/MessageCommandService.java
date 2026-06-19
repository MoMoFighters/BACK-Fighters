package com.wanted.momocity.message.application.service;

import com.wanted.momocity.friend.fmexception.FMBusinessRuleViolationException;
import com.wanted.momocity.friend.fmexception.FMResourceAccessDeniedException;
import com.wanted.momocity.friend.fmexception.FMResourceNotFoundException;
import com.wanted.momocity.friend.infrastructure.persistence.FriendJpaEntity;
import com.wanted.momocity.friend.user.UserWithFMJpaEntity;
import com.wanted.momocity.message.application.command.CreateChatRoomCommand;
import com.wanted.momocity.message.application.command.ReadMessageCommand;
import com.wanted.momocity.message.application.command.SendMessageCommand;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class MessageCommandService implements MessageCommandUseCase {

//    private final MessageSideUserRepository messageSideUserRepository;
//    private final MessageSideFriendRepository messageSideFriendRepository;
    private final MessageRepository messageRepository;
    private final MessageEligibilityPolicy messageEligibilityPolicy;
//    private final SpringDataMessageRepository springDataMessageRepository;
//    private final SpringDataChatRoomMemberRepository springDataChatRoomMemberRepository;
    //notification에 행 추가
    private final ApplicationEventPublisher eventPublisher;
    //웹소켓
    private final ChatRoomSessionManager sessionManager;
    //웹소켓 브로드캐스팅 템플릿 주입
    private final SimpMessagingTemplate messagingTemplate;
//    private final SpringDataChatRoomMemberRepository chatRoomMemberRepository;
//    private final SpringDataChatRoomRepository springDataChatRoomRepository;
//
//    private final SpringDataMessageReadRepository springDataMessageReadRepository;

    //채팅방 조회 및 개설
    @Override
    public CreateRoomView createChatRoomCommandHandle(CreateChatRoomCommand command) {
        log.info("[CreateChatRoomCommandService] 채팅방 조회 및 개설 비즈니스 시작 - 요청자: {}, 방제목:{}, 초대멤버수: {}",
                command.userId(), command.roomTitle(), command.chatMembers().size());

        //리스트에서 상대방 아이디 하나씩 빼기
        List<UserWithFMJpaEntity> targetUsers = new ArrayList<>();
        List<String> friendStatuses = new ArrayList<>();

        for (Long memberId : command.chatMembers()) {
            //초대할 멤버가 존재하지 않는 경우 404
            UserWithFMJpaEntity targetUser = messageRepository.findUserWithFMById(memberId)
                    .orElseThrow(() -> new FMResourceNotFoundException("존재하지 않는 사용자의 대화창을 개설할 수 없습니다."));
            targetUsers.add(targetUser);

            //양방향 친구 관계 조회
            //두 사람 사이의 관계 양방향 조회
            String friendStatus = messageRepository.findFriendRelation(command.userId(), memberId)
                    .map(FriendJpaEntity::getStatus)
                    .orElse("none");
            friendStatuses.add(friendStatus);
        }

        //나와의 채팅 차단, 친구 상태 검증 위임(409), 다대다 규칙
        messageEligibilityPolicy.validateCreate(command.userId(), command.roomTitle(), targetUsers,friendStatuses);

        //다대다 채팅인지 확인
        boolean isOneToOne = (command.roomTitle() == null || command.roomTitle().isEmpty()) && command.chatMembers().size() == 1;
        Long finalRoomId = null;
        UserWithFMJpaEntity singleTargetUser = isOneToOne ? targetUsers.get(0) : null;

        //일대일의 경우만 기존 방 조회 및 멤버 복구
        if (isOneToOne) {
            Long targetUserId = singleTargetUser.getId();

            //1차 검증: 두 유저가 채팅방 멤버에 같이 있는 채팅방이 있는지 조회
            //어댑터 포트를 통해 두 유저가 있는 기존 채팅방이 존재하는지 검증
            Optional<Long> existingRoomIdOpt = messageRepository.findExistingRoom(command.userId(), targetUserId);
            if (existingRoomIdOpt.isPresent()) {
                log.info("[CreateChatRoomCommandService] 1차 멤버 검증 성공 - 양방향 활성화된 채팅방 발견. 기존 방ID: {}", existingRoomIdOpt.get());
                finalRoomId = existingRoomIdOpt.get();
            } else {
                //2차 검증: 로그인한 사용자가 나갔을 때 혼자 남은 방 중 과거 대화 역추적
                log.info("[CreateChatRoomCommandService] 1차 검증 실패(나간 유저 존재) -> 2차 메시지 교차 검증 역추적 시작...");

                //상대방이 참여 중인 모든 멤버 다져옴
                List<ChatRoomMemberJpaEntity> targetMemberships = messageRepository.findChatRoomMembersByUserId(targetUserId);

                for (ChatRoomMemberJpaEntity membership : targetMemberships) {
                    //내가 나간 방일 가능성 있는 후보방
                    Long candidateRoomId = membership.getRoomId().getId();

                    //상대방이 참여 중인 그 방의 인원이 혼자인지 확인
                    List<ChatRoomMemberJpaEntity> roomMembers = messageRepository.findMembersByRoomId(candidateRoomId);

                    if (roomMembers.size() == 1) {
                        //상대방이 혼자 남은 방에 로그인 유저가 보낸 메시지가 1개라도 존재하는 지 확인
                        //로그인 유저가 보낸 메시지가 있다면 로그인 유저가 나간 방
                        boolean hasMyPastMessage = messageRepository.existsMessageByRoomIdAndSenderId(candidateRoomId, command.userId());

                        if (hasMyPastMessage) {
                            finalRoomId = candidateRoomId;
                            log.info("[CreatChatRoomCommandService] 2차 교차 검증 성공 - 로그인 유저가 나갔던 과거 채팅방 발견: {}", finalRoomId);

                            //로그인 유저가 나갔던 방이므로 해당 채팅방 멤버로 복구
                            UserWithFMJpaEntity loginUser = messageRepository.getUserWithFMReferenceById(command.userId());
                            ChatRoomJpaEntity existingRoom = messageRepository.findChatRoomById(finalRoomId)
                                    .orElseThrow(() -> new FMBusinessRuleViolationException("존재하지 않는 채팅방입니다."));

                            ChatRoomMemberJpaEntity myNewMembership = ChatRoomMemberJpaEntity.createMembership(existingRoom, loginUser);
                            messageRepository.saveChatRoomMember(myNewMembership);
                            log.info("[CreateChatRoomCommandService] 나갔던 로그인 유저(ID: {})를 기존 방(ID: {})의 멤버로 복구 완료", command.userId(), finalRoomId);

                            break;
                        }
                    }
                }
            }

            //기존 방 찾았다면 리턴
            if (finalRoomId != null) {
                List<MemberInfo> existingMembers = new ArrayList<>();
                existingMembers.add(new MemberInfo(
                        singleTargetUser.getId(),
                        "TEACHER".equals(singleTargetUser.getRole()) ? singleTargetUser.getName() : null,
                        singleTargetUser.getNickname(),
                        singleTargetUser.getRole(),
                        friendStatuses.get(0)
                ));

                RoomInfo existingRoomInfo = new RoomInfo(
                        finalRoomId,
                        command.roomTitle(),
                        2L //기존 방 복구 시 무조건 일대일이므로 2명 고정
                );

                return new CreateRoomView(
                        true,
                        existingRoomInfo,
                        existingMembers
                );
            }
        }

        //다대다이거나 기존방 없을 때
        //기존 채팅방 없으면 신규 개설
        ChatRoomJpaEntity newRoom = new ChatRoomJpaEntity();
        newRoom.registRoomTitle(isOneToOne ? null : command.roomTitle());//일대일은 null, 다대다는 입력된 제목 지정
        messageRepository.saveChatRoom(newRoom);

        log.info("[CreateChatRoomCommandService] 신규 채팅방 멤버 저장 시작 - 방ID: {}, 요청자: {}, 대상자: {}",
                newRoom.getId(), command.userId(), command.chatMembers());

        //로그인 유저 jpaEntity로 담기
        UserWithFMJpaEntity loginUser = messageRepository.getUserWithFMReferenceById(command.userId());

        //로그인 유저 멤버 저장
        ChatRoomMemberJpaEntity myMembership = ChatRoomMemberJpaEntity.createMembership(newRoom, loginUser);
        messageRepository.saveChatRoomMember(myMembership);

        //상대방 멤버 저장
        for (UserWithFMJpaEntity targetUser: targetUsers) {
            ChatRoomMemberJpaEntity targetMembership = ChatRoomMemberJpaEntity.createMembership(newRoom, targetUser);
            messageRepository.saveChatRoomMember(targetMembership);
        }

        log.info("[CreateChatRoomCommandService] 신규 채팅방 개설 완료 - 방ID: {}", newRoom.getId());

        long inMemberCount = targetUsers.size() + 1; //사용자가 선택한 초대할 멤버수 + 로그인유저

        //멤버 명단
        List<MemberInfo> members = new ArrayList<>();
        for (int i = 0; i < targetUsers.size(); i++) {
            UserWithFMJpaEntity target = targetUsers.get(i);
            String status = friendStatuses.get(i);

            members.add(new MemberInfo(
                    target.getId(),
                    "TEACHER".equals(target.getRole()) ? target.getName() : null,
                    target.getNickname(),
                    target.getRole(),
                    status
            ));
        }

        //생성된 방 정보
        RoomInfo roomInfo = new RoomInfo(
                newRoom.getId(),
                command.roomTitle(),
                inMemberCount //로그인 유저 포함 멤버수이므로 초대된 멤버 + 1
        );

        return new CreateRoomView(
                false,
                roomInfo, //생성된 방 정보
                members //참여자 정보
        );
    }

    //메시지 전송
    @Override
    public SendView sendMessageCommandHandle(SendMessageCommand command) {
        log.info("[SendMessageService] 메시지 전송 프로세스 시작 - 요청자: {}, 방번호: {}", command.senderId(), command.roomId());

        UserWithFMJpaEntity sender = messageRepository.findUserWithFMById(command.senderId())
                .orElseThrow(() -> new FMResourceNotFoundException("존재하지 않는 사용자입니다."));

        //채팅방 조회
        ChatRoomJpaEntity chatRoom = messageRepository.findChatRoomById(command.roomId())
                .orElseThrow(() -> new FMResourceNotFoundException("존재하지 않는 채팅방입니다."));

        //어댑터에서 멤버 테이블 찔러서 현재 방에 소속된 멤버 긁어오기
        List<ChatRoomMemberJpaEntity> members = messageRepository.findMembersByRoomId(command.roomId());
        long roomMemberCount = members.size();

        //상대방 유저 추출
        UserWithFMJpaEntity targetUser = members.stream()
                .map(ChatRoomMemberJpaEntity::getUserId)
                .filter(user -> !user.getId().equals(command.senderId()))
                .findFirst()
                .orElse(sender); //나와의 채팅방일 때 상대방이 없으므로 null

        //상대방과의 친구 관게 조회
        //두 사람 사이의 관계 양방향 조회
        String friendStatus = "none";
        if (targetUser.getId().equals(command.senderId())) { //나와의 채팅방과 나간 채팅방 구분 위함.(나간 채팅방엔 메시지 전송 불가)
            //위에서 상대가 없다면 로그인 유저를 넣었으므로 나와의 채팅방 인식
            friendStatus = "me";
        } else {
            friendStatus = messageRepository.findFriendRelation(command.senderId(), targetUser.getId())
                    .map(FriendJpaEntity::getStatus)
                    .orElse("none");
        }

        messageEligibilityPolicy.sendable(command.roomId(), command.senderId(), friendStatus, roomMemberCount);

        //메시지 테이블에 저장
        MessageJpaEntity newMessage = MessageJpaEntity.createNewMessage(chatRoom, sender, command.content());
        messageRepository.saveMessage(newMessage);

        //읽음 상태 기본 false
        boolean isRead = false;

        //로그인 유저를 제외한 메시지를 받는 사람들 뽑기
        List<MessageReadJpaEntity> readOtherUsers = new ArrayList<>();
        for (ChatRoomMemberJpaEntity member: members) {
            //메시지 보낸 로그인 유저를 수신자 목록에서 제외
            if (member.getUserId().getId().equals(command.senderId())) {
                continue;
            }

            //수신자
            UserWithFMJpaEntity receiver = member.getUserId();

            //웹소켓으로 상대방이 방에 머무는 거 확인 후 있다면 true 처리
            //수신자가 실시간 웹소켓 채널에 접속 중인지 여부 체크
            boolean isUserInRoom = sessionManager.isUserInRoom(receiver.getId(), command.roomId());
            if (isUserInRoom) {
                isRead = true; //상대방이
            }

            //메시지 읽음 테이블에 저장
            MessageReadJpaEntity newUnreadMessage = MessageReadJpaEntity.createNewUnreadMessage(
                    chatRoom,
                    newMessage,
                    receiver,
                    isUserInRoom, //메시지 읽음 여부: 상대방이 채팅방에 머무르면 true, 없으면 false
                    isUserInRoom, //알림 읽음 여부: 상대방이 채팅방에 머무르면 true, 없으면 false
                    isUserInRoom //알림 삭제 여부: 상대방이 채팅방에 머무르면 알림이 없으므로 ture, 없으면 false
            );

            readOtherUsers.add(newUnreadMessage);

            //웹소켓에서도 친구 관계가 아니라면 '닉네임(알 수 없음)'으로 데이터 가공
            String relationWithReceiver = messageRepository.findFriendRelation(command.senderId(), receiver.getId())
                    .map(FriendJpaEntity::getStatus)
                    .orElse("none");

            //웹소켓 시 친구 상태가 아니라면 알 수 없음
            String displayNickname = sender.getNickname();
            if (!"FRIEND".equals(relationWithReceiver)) {
                displayNickname += "(알 수 없음)";
            }

            //실시간 웹소켓 전송(프론트엔트가 구독 중인 주소로 메시지 주머니 투척)
            MessageCommandService.WebSocketMessageDto wsPayload = new MessageCommandService.WebSocketMessageDto(
                newMessage.getId(),
                command.senderId(),
                "TEACHER".equals(sender.getRole()) ? sender.getName() : null,
                displayNickname,
                sender.getRole(),
                command.content(),
                newMessage.getCreatedAt(),
                isRead
            );

            //WebSocketConfig에서 설정한 prefix "/sub" 채널로 발송
            String destination = "/sub/chat/room/" + command.roomId();
            messagingTemplate.convertAndSendToUser(receiver.getId().toString(), destination, wsPayload);
        }

        //루프가 끝난 후 모아둔 읽음 행들 모두 저장
        if (!readOtherUsers.isEmpty()) {
            messageRepository.saveAllMessageRead(readOtherUsers);
        }

        // 🎯 2. 나와의 채팅방이 아닐 때만 상대방(targetUser)에게 알림 이벤트 발행
        for (MessageReadJpaEntity readEntity : readOtherUsers) {
            log.info("[SendMessageService] 메시지 전송 성공 - 알림 발행. 수신자: {}", targetUser.getId());
            eventPublisher.publishEvent(new SendMessagePublishedEvent(
                    command.roomId(),
                    command.senderId(),
                    sender.getNickname(),   // 발신자 닉네임 추출
                    readEntity.getUserId(),
                    newMessage.getCreatedAt()
            ));
        }


        //로그인 유저(보낸 사람) 화면에도 실시간으로 말풍선을 띄위기 위함
        WebSocketMessageDto myPayload = new WebSocketMessageDto(
                newMessage.getId(),
                command.senderId(),
                "TEACHER".equals(sender.getRole()) ? sender.getName() : null,
                sender.getNickname(),
                sender.getRole(),
                command.content(),
                newMessage.getCreatedAt(),
                isRead
        );
        messagingTemplate.convertAndSendToUser(command.senderId().toString(), "/sub/chat/room/" + command.roomId(), myPayload);
        log.info("[웹소켓 개별 분할 발송] 방번호 {} 참여자별 실시간 메시지 브로드캐스팅 완료", command.roomId());


        return new SendView(
                command.roomId(),
                targetUser.getId(),
                targetUser.getNickname(),
                targetUser.getRole(),
                friendStatus,
                command.content(),
                newMessage.getCreatedAt()
        );
    }

    // 🎯 웹소켓 전송용 가벼운 내부 레코드(DTO) 생성
    public record WebSocketMessageDto(
            Long messageId,
            Long senderId,
            String name,
            String nickname,
            String role,
            String content,
            java.time.LocalDateTime createdAt,
            boolean isRead
    ) {}

    //메시지 읽음
    @Override
    public ReadView readMessageCommandHandle(ReadMessageCommand command) {

        //방 존재 검증
        if (!messageRepository.existsRoomById(command.roomId())) {
            throw new FMResourceNotFoundException("존재하지 않거나 삭제된 채팅방입니다.");
        }

        //권한 체크를 위해 멤버 여부 조회
        boolean isCurrentMember = messageRepository.existsMemberByRoomIdAndUserId(command.roomId(), command.userId());

        //권한 체크(방 멤버가 맞는지)
        messageEligibilityPolicy.validateAccess(command.roomId(), command.userId(), isCurrentMember);

        //상대방 닉네임 추출
        List<ChatRoomMemberJpaEntity> members = messageRepository.findMembersByRoomId(command.roomId());
        UserWithFMJpaEntity targetUser = members.stream()
                .map(ChatRoomMemberJpaEntity::getUserId)
                .filter(user -> !user.getId().equals(command.userId()))
                .findFirst()
                .orElse(members.get(0).getUserId()); //나와의 채팅방 대비

        //이 채팅방에서 상대방이 보낸 메시지 중 안읽은 메시지 뽑기
        //v2->읽음 테이블에서 userId가 로그인 유저이면서 메시지 안읽음 여부 확인(메시지 안읽고 알림만 읽은 경우 고려)
        List<MessageReadJpaEntity> unreadMessages = messageRepository.findUnreadMessages(command.roomId(), command.userId());

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
                command.roomId(),
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
        ChatRoomJpaEntity chatRoom = messageRepository.findChatRoomById(roomId)
                .orElseThrow(() -> new FMResourceNotFoundException("존재하지 않거나 이미 나간 채팅방입니다."));

        //해당 방에 들어있는 모든 멤버 가져오기
        List<ChatRoomMemberJpaEntity> allMembers = messageRepository.findMembersByRoomId(roomId);

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
            messageRepository.deleteMessagesByRoomId(roomId); //안내 문구, 읽음 여부, 메시지 삭제
            messageRepository.deleteChatRoomMember(myMembership);
            messageRepository.deleteChatRoom(chatRoom);

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
        messageRepository.deleteChatRoomMember(myMembership);

        // 🎯 [수정]: 전체 멤버 중 '내가 아닌 사람(남겨진 사람)'을 정확히 찾아옵니다.
        // 이러면 나중에 다대다로 확장되어도 최소한 남은 사람 중 한 명을 안전하게 찝어올 수 있습니다.
        UserWithFMJpaEntity targetUser = null;
        for (ChatRoomMemberJpaEntity member : allMembers) {
            if (!member.getUserId().getId().equals(userId)) {
                targetUser = member.getUserId();
                break;
            }
        }

        String friendStatus = messageRepository.findFriendRelation(userId, targetUser.getId())
                .map(FriendJpaEntity::getStatus)
                .orElse("none");

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
