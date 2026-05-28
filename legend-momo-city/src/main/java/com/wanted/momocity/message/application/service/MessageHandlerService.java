package com.wanted.momocity.message.application.service;

import com.wanted.momocity.friend.user.UserWithFMJpaEntity;
import com.wanted.momocity.message.application.policy.MessageEligibilityPolicy;
import com.wanted.momocity.message.domain.repository.MessageRepository;
import com.wanted.momocity.message.infrastructure.persistence.ChatRoomJpaEntity;
import com.wanted.momocity.message.infrastructure.persistence.ChatRoomMemberJpaEntity;
import com.wanted.momocity.message.infrastructure.persistence.MessageSideUserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class MessageHandlerService {

    private final MessageRepository messageRepository;
    private final MessageSideUserRepository messageSideUserRepository;
    private final MessageEligibilityPolicy messageEligibilityPolicy;

    //회원가입 성공 후 날라온 이벤트로 나와의 채팅방 최초 1회 생성
    public void createSelfChatRoom(Long userId) {
        log.info("[MessageHandlerService] 나와의 채팅방 개설 시작 - 대상 유저ID: {}", userId);

        //회원가입 직후 이미 방이 파진 흔적이 있다면 중복 생성이므로 넘어감
        if (messageEligibilityPolicy.isSelfChatRoomExists(userId)) {
            log.warn("[MessageHandlerService] 이미 나와의 채팅방이 존재하여 생성을 건너뜀. (중복 이벤트 방어)");
            return;
        }

        //신규 채팅방 개설(생성 시간 포함)
        ChatRoomJpaEntity selfRoom = new ChatRoomJpaEntity();
        messageRepository.saveChatRoom(selfRoom);

        //가입 완료된 내 유저 정보
        UserWithFMJpaEntity me = messageSideUserRepository.getReferenceById(userId);

        //채팅방에 멤버 딱 한 명
        ChatRoomMemberJpaEntity selfMembership = ChatRoomMemberJpaEntity.createMembership(selfRoom, me);
        messageRepository.saveChatRoomMember(selfMembership);

        log.info("[MessageHandlerService] 나와의 채팅방 생성 및 멤버 저장 완료 - 방ID: {}, 유저ID: {}", selfRoom.getId(), me.getId());
    }
}
