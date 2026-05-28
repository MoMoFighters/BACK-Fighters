package com.wanted.momocity.message.application.service;

import com.wanted.momocity.friend.fmexception.FMResourceNotFoundException;
import com.wanted.momocity.friend.infrastructure.persistence.FriendJpaEntity;

import com.wanted.momocity.friend.user.UserWithFMJpaEntity;
import com.wanted.momocity.message.application.policy.MessageEligibilityPolicy;
import com.wanted.momocity.message.application.usecase.CreateChatRoomCommandUseCase;
import com.wanted.momocity.message.domain.repository.MessageRepository;
import com.wanted.momocity.message.infrastructure.persistence.ChatRoomJpaEntity;
import com.wanted.momocity.message.infrastructure.persistence.ChatRoomMemberJpaEntity;
import com.wanted.momocity.message.infrastructure.persistence.MessageSideFriendRepository;
import com.wanted.momocity.message.infrastructure.persistence.MessageSideUserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.transform.Result;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class CreateChatRoomCommandService implements CreateChatRoomCommandUseCase {

    private final MessageSideUserRepository messageSideUserRepository;
    private final MessageSideFriendRepository messageSideFriendRepository;
    private final MessageRepository messageRepository;
    private final MessageEligibilityPolicy messageEligibilityPolicy;

    //채팅방 조회 및 개설
    @Override
    public CreateRoomView handle(Long userId, Long targetUserId) {
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

        //어댑터 포트를 통해 두 유저가 있는 기존 채팅방이 존재하는지 검증
        Optional<Long> existingRoomIdOpt = messageRepository.findExistingRoom(userId, targetUserId);
        if (existingRoomIdOpt.isPresent()) {
            log.info("[CreateChatRoomCommandService] 기존 채팅방 발견 완료 - 기존 방ID: {}", existingRoomIdOpt.get());
            return new CreateRoomView(
                    true,
                    existingRoomIdOpt.get(),
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
}
