package com.wanted.momocity.message.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SpringDataMessageAnnounceRepository extends JpaRepository<MessageAnnounceJpaEntity, Long> {


    //채팅방 목록 정렬을 위한 안내 문구 시간 확인
    Optional<MessageAnnounceJpaEntity> findFirstByRoomId_IdOrderByCreatedAtDesc(Long roomId);

    //채팅방 나가기: 안내 문구 삭제
    void deleteByRoomId_Id(Long roomId);

    //안내 문구 내역 조회(재입장 고려)
    static List<MessageAnnounceJpaEntity> findByRoomId_IdAndCreatedAtGreaterThanEqual(Long roomId, LocalDateTime startTimeLine) {

    }
}
