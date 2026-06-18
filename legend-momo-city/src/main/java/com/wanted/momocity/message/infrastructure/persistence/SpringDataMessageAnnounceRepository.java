package com.wanted.momocity.message.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataMessageAnnounceRepository extends JpaRepository<MessageAnnounceJpaEntity, Long> {

    //채팅방 목록 정렬을 위한 안내 문구 시간 확인
    Optional<MessageAnnounceJpaEntity> findFirstByRoomId_IdOrderByCreatedAtDesc(Long roomId);
}
