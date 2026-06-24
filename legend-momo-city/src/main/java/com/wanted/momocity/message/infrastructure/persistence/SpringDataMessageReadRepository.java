package com.wanted.momocity.message.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataMessageReadRepository extends JpaRepository<MessageReadJpaEntity, Long> {

    //채팅방 목록 안읽음 개수
    Long countByRoomId_IdAndUserId_IdAndIsMsgReadFalse(Long roomId, Long userId);

    //메시지 읽음 처리
    List<MessageReadJpaEntity> findByRoomId_IdAndUserId_IdAndIsMsgReadFalse(Long roomId, Long userId);

    //채팅방 가가기: 읽음 여부 삭제
    void deleteByRoomId_Id(Long roomId);

    //하나의 메시지당 읽지 않은 멤버 수
    Long countByMessageId_IdAndIsMsgReadFalse(Long messageId);
}
