package com.wanted.momocity.notification.infrastructure.persistence;

import com.wanted.momocity.message.infrastructure.persistence.ChatRoomJpaEntity;
import com.wanted.momocity.message.infrastructure.persistence.MessageReadJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationSideMessageReadRepository extends JpaRepository<MessageReadJpaEntity, Long> {

    //총 알림 개수(메인 페이지 종)
    //읽지 않고 삭제되지 않는 메시지가 있는 채팅방의 수 = 알림 목록에 있는 메시지 알림 수
    @Query("SELECT COUNT(DISTINCT mr.roomId.id) FROM MessageReadJpaEntity mr " +
            "WHERE mr.userId.id = :userId " +
            "AND mr.isNotiRead = false " +
            "AND mr.isDeleted = false")
    long countUnreadMessageRooms(@Param("userId") Long userId);

    //휴대폰 속 앱별 알림 개수(메시지) - isMsgRead가 false인 것.
    @Query("SELECT COUNT(mr) FROM MessageReadJpaEntity mr WHERE mr.userId.id = :userId AND mr.isMsgRead = false")
    long countByUserIdAndIsMsgReadFalse(@Param("userId") Long userId);
}
