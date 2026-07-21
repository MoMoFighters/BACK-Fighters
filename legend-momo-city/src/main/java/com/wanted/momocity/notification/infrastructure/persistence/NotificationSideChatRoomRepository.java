package com.wanted.momocity.notification.infrastructure.persistence;

import com.wanted.momocity.message.infrastructure.persistence.ChatRoomJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NotificationSideChatRoomRepository extends JpaRepository<ChatRoomJpaEntity, Long> {

    //알림 목록 - 방 이름
    @Query("SELECT cr.roomTitle FROM ChatRoomJpaEntity cr WHERE cr.id = :roomId")
    Optional<String> findTitleById(@Param("roomId") Long roomId);

    // [성능 치트키] 루프 안에서 단건 조회하지 않도록 여러 방의 타이틀을 한 번에 가져오는 쿼리 추가
    @Query("SELECT cr.id, cr.roomTitle FROM ChatRoomJpaEntity cr WHERE cr.id IN :roomIds")
    List<Object[]> findTitlesByRoomIdsIn(@Param("roomIds") List<Long> roomIds);
}
