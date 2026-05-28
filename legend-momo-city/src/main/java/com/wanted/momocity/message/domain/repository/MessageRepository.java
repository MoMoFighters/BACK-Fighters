package com.wanted.momocity.message.domain.repository;

import com.wanted.momocity.friend.infrastructure.persistence.FriendJpaEntity;
import com.wanted.momocity.message.infrastructure.persistence.ChatRoomJpaEntity;
import com.wanted.momocity.message.infrastructure.persistence.ChatRoomMemberJpaEntity;
import com.wanted.momocity.message.infrastructure.persistence.MessageJpaEntity;

import java.util.List;
import java.util.Optional;

//포트 역할
public interface MessageRepository {

    //로그인한 사용자가 속한 모든 채팅방의 상대방 정보 및 마지막 메시지 내역 긁어오기
    List<ChatRoomQueryProjection> findChatRoomByUserId(Long userId);

    //안 읽은 카운트 포트 개발
    Long countUnreadMessage(Long roomId, Long userId);

    //채팅방 개설 시 기존 채팅방 존재 여부 확인
    Optional<Long> findExistingRoom(Long userId, Long targetUserId);
    //신규 방 보관용 포트
    void saveChatRoom(ChatRoomJpaEntity room);
    //신규 멤버 참여 보관용 포트
    void saveChatRoomMember(ChatRoomMemberJpaEntity member);

    //채팅방 찾기
    Optional<ChatRoomJpaEntity> findChatRoomById(Long roomId);

    //멤버 찾기
    List<ChatRoomMemberJpaEntity> findMembersByRoomId(Long roomId);

    void saveMessage(MessageJpaEntity newMessage);
}
