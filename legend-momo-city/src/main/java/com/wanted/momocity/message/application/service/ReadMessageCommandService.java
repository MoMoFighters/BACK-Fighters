package com.wanted.momocity.message.application.service;


import com.wanted.momocity.friend.fmexception.FMResourceAccessDeniedException;
import com.wanted.momocity.friend.fmexception.FMResourceNotFoundException;
import com.wanted.momocity.friend.user.UserWithFMJpaEntity;
import com.wanted.momocity.message.application.usecase.ReadMessageCommandUseCase;
import com.wanted.momocity.message.infrastructure.persistence.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ReadMessageCommandService implements ReadMessageCommandUseCase {

    private final SpringDataChatRoomMemberRepository chatRoomMemberRepository;
    private final SpringDataMessageRepository messageRepository;
    private final SpringDataChatRoomRepository chatRoomRepository;

    //메시지 읽음
    @Override
    public ReadView handle(Long roomId, Long userId) {

        //방 존재 검증
        boolean existsRoom = chatRoomRepository.existsById(roomId);
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
        List<MessageJpaEntity> unreadMessages = messageRepository.findByRoomId_IdAndSenderId_IdAndIsReadFalse(roomId, targetUser.getId());

        //안읽은 메시지 리스트가 비어있는지 체크
        boolean hasUnread = !unreadMessages.isEmpty();

        //반복문 돌면서 상태를 true로 변경
        if (hasUnread) {
            for (MessageJpaEntity message : unreadMessages) {
                message.changeIsRead(true);
            } log.info("[ReadMessageCommandService] 상대방 메시지 {}건 읽음 처리 완료", unreadMessages.size());
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
}
