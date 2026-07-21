package com.wanted.momocity.message.application.service;

import com.wanted.momocity.friend.fmexception.FMBusinessRuleViolationException;
import com.wanted.momocity.friend.fmexception.FMResourceNotFoundException;
import com.wanted.momocity.friend.infrastructure.persistence.FriendJpaEntity;
import com.wanted.momocity.friend.user.UserWithFMJpaEntity;
import com.wanted.momocity.message.application.command.*;
import com.wanted.momocity.message.application.manager.ChatRoomSessionManager;
import com.wanted.momocity.message.application.metric.MessageMetrics;
import com.wanted.momocity.message.application.policy.MessageEligibilityPolicy;
import com.wanted.momocity.message.application.usecase.MessageCommandUseCase;
import com.wanted.momocity.message.application.usecase.MessageQueryUseCase;
import com.wanted.momocity.message.domain.event.*;
import com.wanted.momocity.message.domain.repository.MessageRepository;
import com.wanted.momocity.message.infrastructure.persistence.*;
import com.wanted.momocity.notification.application.usecase.NotificationQueryUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class MessageCommandService implements MessageCommandUseCase {

    private final MessageRepository messageRepository;
    private final MessageEligibilityPolicy messageEligibilityPolicy;
    private final ApplicationEventPublisher eventPublisher;
    //웹소켓
    private final ChatRoomSessionManager sessionManager;
    //웹소켓 브로드캐스팅 템플릿 주입
    private final SimpMessagingTemplate messagingTemplate;

    //웹소켓 중복 가공 회피를 위한 QueryUseCase 주입
    private final MessageQueryUseCase messageQueryUseCase;

    //웹소켓 알림 관련
    private final NotificationQueryUseCase notificationQueryUseCase;

    //메트릭
    private final MessageMetrics messageMetrics;

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
        messageEligibilityPolicy.validateCreate(command.userId(), command.roomTitle(), targetUsers, friendStatuses);

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
                finalRoomId = existingRoomIdOpt.get();
                log.info("[CreateChatRoomCommandService] 1차 멤버 검증 성공 - 양방향 활성화된 채팅방 발견. 기존 방ID: {}", existingRoomIdOpt.get());

                List<MemberInfo> existingMembers = new ArrayList<>();
                existingMembers.add(new MemberInfo(
                        singleTargetUser.getId(),
                        "TEACHER".equals(singleTargetUser.getRole()) ? singleTargetUser.getName() : null,
                        singleTargetUser.getNickname(),
                        singleTargetUser.getRole(),
                        friendStatuses.get(0)
                ));

                // 🎯 딱 한 줄: 기존 방 조회 완료 시점에도 멤버 수(2명) 분포 기록
                messageMetrics.recordRoomMemberCount(2.0);
                RoomInfo existingRoomInfo = new RoomInfo(finalRoomId, null, 2L); // 일대일이므로 방제목은 확실하게 null 보장

                return new CreateRoomView(true, existingRoomInfo, existingMembers);
            } else {
                //2차 검증: 로그인한 사용자가 나갔을 때 혼자 남은 방 중 과거 대화 역추적
                log.info("[CreateChatRoomCommandService] 1차 검증 실패(나간 유저 존재) -> 2차 메시지 교차 검증 역추적 시작...");

                // 🎯 [SQL 최적화]: 루프 내 N+1 쿼리를 막기 위해, 상대방이 참여 중인 1인실(나만 퇴장한 일대일 방) 목록을 한 방 쿼리로 먼저 가져옵니다.
                List<ChatRoomMemberJpaEntity> singleCandidateMemberships = messageRepository.findOnePersonRoomsByUserId(targetUserId, command.userId());

                for (ChatRoomMemberJpaEntity membership : singleCandidateMemberships) {
                    //내가 나간 방일 가능성 있는 후보방
                    Long candidateRoomId = membership.getRoomId().getId();
                    ChatRoomJpaEntity room = membership.getRoomId(); //방 엔티티 꺼내기

                    //로그인 유저 객체
                    UserWithFMJpaEntity loginUser = messageRepository.getUserWithFMReferenceById(command.userId());

                    //상대방이 혼자 남은 방에 로그인 유저가 보낸 메시지가 1개라도 존재하는 지 확인
                    //로그인 유저가 보낸 메시지가 있다면 로그인 유저가 나간 방 + 안내 문구 확인
                    boolean hasMyPastMessage = messageRepository.existsMessageByRoomIdAndSenderId(candidateRoomId, command.userId())
                            || messageRepository.existsAnnounceByRoomIdAndTargetId(room, loginUser);

                    if (hasMyPastMessage) {
                        finalRoomId = candidateRoomId;
                        log.info("[CreatChatRoomCommandService] 2차 교차 검증 성공 - 로그인 유저가 나갔던 과거 채팅방 발견: {}", finalRoomId);

                        //로그인 유저가 나갔던 방이므로 해당 채팅방 멤버로 복구
                        ChatRoomJpaEntity existingRoom = messageRepository.findChatRoomById(finalRoomId)
                                .orElseThrow(() -> new FMBusinessRuleViolationException("존재하지 않는 채팅방입니다."));

                        ChatRoomMemberJpaEntity myNewMembership = ChatRoomMemberJpaEntity.createMembership(existingRoom, loginUser);
                        messageRepository.saveChatRoomMember(myNewMembership);
                        log.info("[CreateChatRoomCommandService] 나갔던 로그인 유저(ID: {})를 기존 방(ID: {})의 멤버로 복구 완료", command.userId(), finalRoomId);

                        //재입장 시 message_announce 테이블에 안내 문구 추가
                        messageRepository.saveEnterAnnounce(existingRoom, loginUser, loginUser.getNickname() + "님이 입장했습니다.");
                        log.info("[CreatedChatRoomCommandService] 재입장 안내 문구 추가 완료. 방ID:{}", finalRoomId);

                        messageRepository.fastSaveChanges();

                        // 🎯 백그라운드로 무거운 무한 루프 조회 및 웹소켓 전송 이관
                        eventPublisher.publishEvent(new ChatRoomReenteredPublishedEvent(finalRoomId, List.of(command.userId(), targetUserId)));
                        log.info("[CreateChatRoomCommandService] 과거 방 복구 완료에 따른 웹소켓 전송 이벤트 발행 성공");

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
                                null,
                                2L //기존 방 복구 시 무조건 일대일이므로 2명 고정
                        );

                        // 🎯 딱 두 줄: 재입장 특수 트래픽 발생 카운트 증가 + 복구 방 멤버 수(2명) 기록
                        messageMetrics.incrementChatReenterCount();
                        messageMetrics.recordRoomMemberCount(2.0);

                        return new CreateRoomView(
                                true,
                                existingRoomInfo,
                                existingMembers
                        );
                    }
                }
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

        // 초대된 상대방들의 멤버십 저장 및 웹소켓 대상자 ID 수집
        List<Long> allMemberIds = new ArrayList<>();
        allMemberIds.add(command.userId()); // 나 포함

        //상대방 멤버 저장
        for (UserWithFMJpaEntity targetUser: targetUsers) {
            ChatRoomMemberJpaEntity targetMembership = ChatRoomMemberJpaEntity.createMembership(newRoom, targetUser);
            messageRepository.saveChatRoomMember(targetMembership);
            allMemberIds.add(targetUser.getId()); // 상대방들 추가
        }

        messageRepository.fastSaveChanges();
        // 신규 채팅방 생성 완료에 따른 참여자 전원 화면 리로드 웹소켓 비동기 이벤트 발행!
        // ReenteredPublishedEvent를 공용으로 재활용하셔도 좋고, 별도 개설 이벤트(RoomCreatedPublishedEvent)를 파셔도 좋습니다.
        eventPublisher.publishEvent(new ChatRoomReenteredPublishedEvent(newRoom.getId(), allMemberIds));
        log.info("[CreateChatRoomCommandService] 신규 채팅방 개설 완료 및 웹소켓 전송 이벤트 발행 성공 - 방ID: {}", newRoom.getId());

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

        // 신규 개설 방의 멤버 수 분포 수집
        messageMetrics.recordRoomMemberCount((double) inMemberCount);

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

        //나와의 채팅방 식별: 로그인 유저의 최초의 방
        Long firstRoomId = messageRepository.findFirstRoomIdByUserId(command.senderId())
                .orElse(null);

        //어댑터에서 멤버 테이블 찔러서 현재 방에 소속된 멤버 긁어오기
        List<ChatRoomMemberJpaEntity> members = messageRepository.findMembersByRoomId(command.roomId());
        long roomMemberCount = members.size();

        //일대일, 다대다 여부
        boolean isOneToOne = chatRoom.getRoomTitle() == null || chatRoom.getRoomTitle().trim().isEmpty();

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

        messageEligibilityPolicy.sendable(command.roomId(), command.senderId(), friendStatus, roomMemberCount, isOneToOne, targetUser, firstRoomId);

        //메시지 테이블에 저장
        MessageJpaEntity newMessage = MessageJpaEntity.createNewMessage(chatRoom, sender, command.content());
        messageRepository.saveMessage(newMessage);

        // 메시지가 성공적으로 적재 및 유효 통과 시점에 글로벌 TPS 카운트 증가
        messageMetrics.incrementMessageSendCount();

        List<MessageReadJpaEntity> readOtherUsers = new ArrayList<>();

        for (ChatRoomMemberJpaEntity member : members) {
            //로그인 유저가 보낸 메시지라면 그대로
            if (member.getUserId().getId().equals(command.senderId())) continue;

            //메시지 받는 사람
            UserWithFMJpaEntity receiver = member.getUserId();
            //메시지 받는 사람이 현재 방에 머무는지
            boolean isUserInRoom = sessionManager.isUserInRoom(receiver.getId(), command.roomId());

            MessageReadJpaEntity newUnreadMessage = MessageReadJpaEntity.createNewUnreadMessage(
                    chatRoom,
                    newMessage,
                    receiver,
                    isUserInRoom,
                    isUserInRoom,
                    false
            );
            readOtherUsers.add(newUnreadMessage);
        }

        //현재 채팅방에 머무는 사람이 있으면 읽음 처리
        if (!readOtherUsers.isEmpty()) {
            messageRepository.saveAllMessageRead(readOtherUsers);
        }

        //데이터 정합성을 위한 영속성 플러시
        // (새로 데이터를 전송하고 밑에서 채팅방에 머무르는 사람있으면 저장되기 전에 메시지 내역이 호출됨)
        messageRepository.fastSaveChanges();

        //위에서 상대가 화면에 머무르는지 여부로 읽음 여부를 설정했으므로 재활용
        // 수신자들 중 '안 읽은 사람(방에 없는 사람)'이 한 명이라도 존재하는지 검증
        boolean hasUnreadReceiver = readOtherUsers.stream()
                .anyMatch(mr -> !mr.isMsgRead()); // 하나라도 안 읽었으면(false) true 반환

        //웹소켓으로 채팅방에 머무르는 사람이 없을 때만 notification테이블에 행 생성
        if (hasUnreadReceiver) {
            //나와의 채팅방이 아닐 때만 상대방(targetUser)에게 알림 이벤트 발행
            if (!targetUser.getId().equals(command.senderId())) {
                log.info("[SendMessageService] 메시지 전송 성공 - 알림 발행. 방번호: {}", command.roomId());
                eventPublisher.publishEvent(new SendMessagePublishedEvent(
                        command.roomId(),
                        chatRoom.getRoomTitle(),
                        command.senderId(),
                        sender.getNickname(),   // 발신자 닉네임 추출
                        isOneToOne ? targetUser.getId() : null, //일대일이면 상대방 정보 전달, 다대다면 null 처리
                        newMessage.getCreatedAt()
                ));
            }
        } else {
            log.info("[SendMessageService] 모든 참여자가 방에 상주 중이므로 알림 생성을 건너뜁니다.");
        }

        List<Long> currentMemberIds = members.stream()
                .map(m -> m.getUserId().getId())
                .toList();

        eventPublisher.publishEvent(new ChatMessageSentWebsocketPublishedEvent(command.roomId(), command.senderId(), currentMemberIds));
        log.info("[SendMessageService] 메시지 적재 완료 및 실시간 화면 갱신 비동기 이벤트 발행 성공");

        return new SendView(
                command.roomId(),
                isOneToOne ? targetUser.getId() : null,
                isOneToOne ? targetUser.getNickname() : chatRoom.getRoomTitle(),
                isOneToOne ? targetUser.getRole() : null,
                isOneToOne ? friendStatus : "none",
                command.content(),
                newMessage.getCreatedAt()
        );
    }


    //메시지 읽음
    @Override
    public ReadView readMessageCommandHandle(ReadMessageCommand command) {
        log.info("[ReadMessageCommandService] 읽음 처리 시작 - 요청자: {}, 방번호: {}", command.userId(), command.roomId());

        //방제목 알아야 함(응답 메시지에서 다대다 분기를 위함)
        //방 존재 검증
        ChatRoomJpaEntity chatRoom = messageRepository.findChatRoomById(command.roomId())
                .orElseThrow(() -> new FMResourceNotFoundException("존재하지 않거나 삭제된 채팅방입니다."));

        //권한 체크를 위해 멤버 여부 조회
        boolean isCurrentMember = messageRepository.existsMemberByRoomIdAndUserId(command.roomId(), command.userId());

        //권한 체크(방 멤버가 맞는지)
        messageEligibilityPolicy.validateAccess(command.roomId(), command.userId(), isCurrentMember);

        //일대일, 다대다 분가
        boolean isOneToOne = chatRoom.getRoomTitle() == null ||chatRoom.getRoomTitle().trim().isEmpty();

        UserWithFMJpaEntity targetUser = null;

        //상대방 닉네임 추출(일대일인 경우)
        List<ChatRoomMemberJpaEntity> members = messageRepository.findMembersByRoomId(command.roomId()); //비동기 아이디 추출을 위해 상단으로 올림
        if (isOneToOne) {
            targetUser = members.stream()
                    .map(ChatRoomMemberJpaEntity::getUserId)
                    .filter(user -> !user.getId().equals(command.userId()))
                    .findFirst()
                    .orElse(members.get(0).getUserId()); //나와의 채팅방 대비
        }

        // 벌크 연산 튜닝 적용: 자바 루프 없이, 단 1방의 UPDATE 쿼리로 수백 건 일괄 읽음 처리 완료!
        int updatedCount = messageRepository.bulkUpdateReadStatus(command.roomId(), command.userId());
        boolean hasUnread = updatedCount > 0; // 바뀐 행이 1개라도 있다면 안 읽은 메시지가 존재했던 것

        if (hasUnread) {
            log.info("[ReadMessageCommandService] 벌크 연산 성공 - 상대방 메시지 총 {}건 일괄 읽음/알림 통합 처리 완료", updatedCount);
        } else {
            log.info("[ReadMessageCommandService] 읽을 메시지가 존재하지 않아 기존 상태 유지");
        }

        //[웹소켓 실시간 발송 추가]
        // 1. 상태 변경 사항을 DB와 완전히 동기화 시키기 위해 영속성 플러시 실행
        messageRepository.fastSaveChanges();

        //[컴파일 에러 해결]: 주석 처리되었던 멤버 ID 수집 로직을 안전하게 복구합니다.
        List<Long> currentMemberIds = members.stream()
                .map(m -> m.getUserId().getId())
                .toList();

        // 지옥의 무한 쿼리 루프(대화방 리로드, 목록 리로드, 배지 카운트 갱신)를 백그라운드로 전면 이관
        eventPublisher.publishEvent(new ChatRoomReadWebsocketPublishedEvent(command.roomId(), command.userId(), currentMemberIds));
        log.info("[ReadMessageCommandService] 읽음 처리 완료 및 실시간 화면 갱신 비동기 이벤트 발행 성공");


        return new ReadView(
                command.roomId(),
                isOneToOne ? targetUser.getId() : null,
                isOneToOne ? targetUser.getNickname() : chatRoom.getRoomTitle(),
                hasUnread
        );
    }

    //채팅방 나가기
    @Override
    public LeaveChatRoomView leaveChatRoomCommandHandle(LeaveChatRoomCommand command) {
        log.info("[LeaveChatRoomCommandService] 채팅방 나가기 검증 시작 - 방ID: {}, 유저ID: {}", command.roomId(), command.userId());

        //나가려는 채팅방이 존재하는 지 검증(404)
        ChatRoomJpaEntity chatRoom = messageRepository.findChatRoomById(command.roomId())
                .orElseThrow(() -> new FMResourceNotFoundException("존재하지 않거나 이미 나간 채팅방입니다."));

        //해당 방에 들어있는 모든 멤버 가져오기
        List<ChatRoomMemberJpaEntity> allMembers = messageRepository.findMembersByRoomId(command.roomId());

        //정책 위임(403, 409)
        messageEligibilityPolicy.leaveChatRoom(command.userId(), command.roomId(), allMembers);

        //로그인 유저 정보 추출
        ChatRoomMemberJpaEntity myMembership = null;
        for (ChatRoomMemberJpaEntity member: allMembers) {
            if (member.getUserId().getId().equals(command.userId())) {
                myMembership = member;
                break;
            }
        }

        UserWithFMJpaEntity loginUser = messageRepository.getUserWithFMReferenceById(command.userId());

        //방 분기
        long currentMemberCount = allMembers.size();

        //로그인 유저가 방에 남은 마지막 사용자일 때(chat_room, chat_room_member, message 모두 삭제)
        if (currentMemberCount <= 1) {
            log.info("[LeaveChatRoomCommandService] 마지막 사용자 퇴장 처리 -> 방 폭파 진행");
            messageRepository.deleteMessagesByRoomId(command.roomId()); //안내 문구, 읽음 여부, 메시지 삭제
            messageRepository.deleteChatRoomMember(myMembership);
            messageRepository.deleteChatRoom(chatRoom);

            return new LeaveChatRoomView(
                    true,
                    command.roomId(),
                    null,
                    null,
                    null,
                    null
            );
        }

        //상대방이 남아있을 때(chat_room_member에서만 삭제)
        log.info("[LeaveChatRoomCommandService] 상대방 존재 확인 -> 로그인 유저 멤버 행만 삭제");
        messageRepository.deleteChatRoomMember(myMembership);

        //message_announce 테이블에 행 추가
        messageRepository.saveLeaveAnnounce(chatRoom, loginUser, loginUser.getNickname() + "님이 나갔습니다.");

        // =========================================================================
        // [핵심 최적화]: 지옥의 웹소켓 화면 갱신 루프를 비동기 이벤트로 이관
        // =========================================================================
        // 남은 사람들의 ID 목록을 먼저 안전하게 추출합니다.
        List<Long> remainingUserIds = allMembers.stream()
                .map(m -> m.getUserId().getId())
                .filter(id -> !id.equals(command.userId()))
                .toList();

        // 메인 트랜잭션 종료(커밋) 직후 백그라운드 스레드에서 무거운 화면 리로드가 돌도록 이벤트를 쏩니다.
        eventPublisher.publishEvent(new ChatRoomLeaveWebsocketPublishedEvent(command.roomId(), command.userId(), remainingUserIds));
        log.info("[LeaveChatRoomCommandService] 퇴장 처리 완료 및 남은 멤버 화면 갱신 비동기 이벤트 발행 성공");
        // =========================================================================

        // [수정]: 전체 멤버 중 '내가 아닌 사람(남겨진 사람)'을 정확히 찾아옵니다.
        // 이러면 나중에 다대다로 확장되어도 최소한 남은 사람 중 한 명을 안전하게 찝어올 수 있습니다.
        UserWithFMJpaEntity targetUser = null;
        String friendStatus = "none";

        //로그인 유저 나간 멤버수
        long remainingCount = allMembers.size() - 1;

        //1명 이상이면 null로 응답, 1명이면 남은 유저 정보 전달
        if (remainingCount == 1) {
            for (ChatRoomMemberJpaEntity member : allMembers) {
                if (!member.getUserId().getId().equals(command.userId())) {
                    targetUser = member.getUserId();
                    break;
                }
            }

            if (targetUser != null) {
                friendStatus = messageRepository.findFriendRelation(command.userId(), targetUser.getId())
                        .map(FriendJpaEntity::getStatus)
                        .orElse("none");
            }
        }


        return new LeaveChatRoomView(
                false,
                command.roomId(),
                remainingCount == 1 ? targetUser.getId() : null,
                remainingCount == 1 && chatRoom.getRoomTitle() == null ? targetUser.getNickname() : chatRoom.getRoomTitle(),
                //남은 인원이 한 명이고 채팅방 이름 없음(일대일)이면 그 사람 닉네임, 아니면 채팅방 이름
                remainingCount == 1? targetUser.getRole() : null,
                remainingCount == 1? friendStatus : null
        );
    }

    //다대다일 때 채팅방 이름 바꾸기
    @Override
    public ModifyRoomTitleView modifyRoomTitleCommandHandle(ModifyRoomTitleCommand command) {

        //존재하지 않는 채팅방
        ChatRoomJpaEntity chatRoom = messageRepository.findChatRoomById(command.roomId())
                .orElseThrow(() -> new FMResourceNotFoundException("존재하지 않는 채팅방입니다."));

        List<ChatRoomMemberJpaEntity> members = messageRepository.findMembersByRoomId(command.roomId());

        // [인메모리 최적화] 공지 및 리턴 객체 조립용 loginUser를 메모리에서 추출 (DB 조회 없음)
        UserWithFMJpaEntity loginUser = members.stream()
                .map(ChatRoomMemberJpaEntity::getUserId)
                .filter(user -> user.getId().equals(command.userId()))
                .findFirst()
                .orElse(null); // 권한 예외는 아래 정책 클래스에 완전히 위임합니다.

        //정책 위임(접근 권한, 다대다 아님, 똑같은 이름, 빈 값, 20자 제한)
        messageEligibilityPolicy.modifyRoomTitle(command.roomId(), command.userId(), command.roomTitle(), chatRoom, members);

        chatRoom.updateRoomTitle(command.roomTitle());

        //message_announce 테이블에 행 추가
        String announceContent = String.format("%s님이 채팅방 이름을 [%s](으)로 변경했습니다.", loginUser.getNickname(), chatRoom.getRoomTitle());
        messageRepository.saveRenameAnnounce(
                chatRoom,
                loginUser,
                announceContent,
                chatRoom.getUpdatedAt());

        messageRepository.fastSaveChanges();

        // 방 안의 모든 참여자(나 포함) ID 목록을 인메모리 스트림으로 추출
        List<Long> memberIds = members.stream()
                .map(m -> m.getUserId().getId())
                .toList();

        // 트랜잭션 정상 커밋 후 백그라운드 스레드에서 무거운 화면 데이터 조회가 돌도록 이벤트 발행
        eventPublisher.publishEvent(new ChatRoomRenamedWebsocketPublishedEvent(chatRoom.getId(), memberIds));
        log.info("[ModifyRoomTitleService] 이름 변경 처리 완료 및 전원 화면 갱신 비동기 이벤트 발행 성공");

        return new ModifyRoomTitleView(
                chatRoom.getId(),
                loginUser.getId(),
                loginUser.getNickname(),
                loginUser.getRole(),
                chatRoom.getRoomTitle(),
                chatRoom.getUpdatedAt()
        );
    }

    //다대다 채팅방 멤버 초대하기
    @Transactional
    public InviteRoomMemberView inviteRoomMemberCommandHandle(InviteRoomMemberCommand command) {
        log.info("[InviteRoomMemberCommandService] 다대다 채팅방 멤버 초대하기 시작 - 방ID:{}, 요청자:{}", command.roomId(), command.userId());
        // 본인 초대 불가
        // 로그인 유저(초대 주체)와 초대 대상자들이 친구인지 검증, 일대일인지 확인, 중복 멤버 확인
        //컨트롤러에 string 처리한 초대 대상자들 닉네임 가공해서 넘기기

        UserWithFMJpaEntity loginUser = messageRepository.findUserWithFMById(command.userId())
                .orElseThrow(() -> new FMResourceNotFoundException("존재하지 않는 사용자입니다."));

        ChatRoomJpaEntity chatRoom = messageRepository.findChatRoomById(command.roomId())
                .orElseThrow(() -> new FMResourceNotFoundException("존재하지 않는 채팅방입니다."));

        //기본 검증(접근 권한, 중복 사용자)
        messageEligibilityPolicy.validateBeforeLoop(chatRoom, command.userId(), command.chatMember());

        // [인메모리 최적화 1]: 현재 방에 있는 기존 멤버들의 유저 ID 목록을 '패치 조인' 메서드로 단 1번만 조회
        List<ChatRoomMemberJpaEntity> existingMembers = messageRepository.findMembersByRoomId(chatRoom.getId());
        Set<Long> existingMemberUserIds = existingMembers.stream()
                .map(m -> m.getUserId().getId())
                .collect(Collectors.toSet());

        // [인메모리 최적화 2]: 초대 타겟 유저 리스트를 IN 쿼리로 한방에 일괄 조회 (반복문 조회 제거)
        List<UserWithFMJpaEntity> invitees = messageRepository.findUsersWithFMByIds(command.chatMember());
        if (invitees.size() != command.chatMember().size()) {
            throw new FMResourceNotFoundException("존재하지 않는 사용자가 포함되어 있어 초대할 수 없습니다.");
        }

        // [인메모리 최적화 3]: 로그인 유저와 초대 대상자들 간의 친구 관계 목록을 IN 쿼리로 한방에 일괄 조회 및 Map 전환
        List<FriendJpaEntity> friendRelations = messageRepository.findFriendRelationsByUserIdAndTargetIds(command.userId(), command.chatMember());
        Set<Long> targetIds = new HashSet<>(command.chatMember());
        Long myId = command.userId();

        Map<Long, String> friendStatusMap = new HashMap<>();
        for (FriendJpaEntity friend : friendRelations) {
            Long fromId = friend.getFromUserId().getId();
            Long toId = friend.getToUserId().getId();
            // 내가 from이면 to가 상대방, 내가 to이면 from이 상대방
            Long opponentId = fromId.equals(myId) ? toId : fromId;

            if (targetIds.contains(opponentId)) {
                friendStatusMap.put(opponentId, friend.getStatus());
            }
        }

        LocalDateTime invitedAt = LocalDateTime.now();

        // 초대 대상자들 존재 확인
        for (UserWithFMJpaEntity invitee : invitees) {
            Long inviteeId = invitee.getId();

            //초대 대상자가 이미 멤버인지 여부
            boolean isExistMember = existingMemberUserIds.contains(inviteeId);

            //초대 대상자에 로그인 유저 포함 여부
            boolean hasMe = invitee.getId().equals(command.userId());

            //초대 멤버 role 학생 아닌지 여부(관리자, 강사) 또는 비활성 유저
            boolean isNotStudentOrActive = !"STUDENT".equals(invitee.getRole()) || !"ACTIVE".equals(invitee.getStatus());

            //초대 대상자들과 로그인 유저가 친구인지 여부
            String friendStatus = friendStatusMap.getOrDefault(inviteeId, "none");

            //정책 위임
            messageEligibilityPolicy.inviteRoomMember(chatRoom, command.userId(), command.chatMember(), hasMe, friendStatus, isExistMember, isNotStudentOrActive);

            //초대한 멤버들 저장
            messageRepository.saveInviteChatRoomMember(chatRoom, invitee, invitedAt);
        }

        //컨트롤러에 필요한 초대된 멤버들 닉네임 가공
        List<String> nicknames = invitees.stream()
                .map(UserWithFMJpaEntity::getNickname)
                .toList();
        String invitedUserNicknames = String.join(",", nicknames);

        //message_announce 테이블에 행 추가
        String inviteMessage = String.format("%s님이 %s님을 초대했습니다.", loginUser.getNickname(), invitedUserNicknames);
        messageRepository.saveInviteAnnounce(
                chatRoom,
                loginUser,
                inviteMessage,
                invitedAt);

        messageRepository.fastSaveChanges();

        // 기존 멤버 ID 리스트에 이번에 새로 추가된 초대 대상자들의 ID까지 합쳐서 전원 발송 대상 추출
        List<Long> allTargetMemberIds = new ArrayList<>(existingMemberUserIds);
        allTargetMemberIds.addAll(command.chatMember());

        // 트랜잭션 커밋 직후 백그라운드 스레드에서 화면 리로드가 돌도록 비동기 이벤트 발행
        eventPublisher.publishEvent(new ChatRoomMemberInviteWebsocketPublishedEvent(chatRoom.getId(), allTargetMemberIds));
        log.info("[InviteRoomMemberService] 멤버 초대 완료 및 전원 화면 갱신 비동기 이벤트 발행 성공");

        return new InviteRoomMemberView(
                chatRoom.getId(),
                chatRoom.getRoomTitle(),
                loginUser.getId(),
                loginUser.getNickname(),
                loginUser.getRole(),
                invitedAt,
                invitedUserNicknames
        );
    }
}
