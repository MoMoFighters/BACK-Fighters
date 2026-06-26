package com.wanted.momocity.notification.infrastructure.persistence;

import com.wanted.momocity.friend.user.UserWithFMJpaEntity;
import com.wanted.momocity.message.infrastructure.persistence.ChatRoomJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface NotificationSideChatRoomRepository extends JpaRepository<ChatRoomJpaEntity, Long> {

    //알림 목록 - 방 이름
    @Query("SELECT cr.roomTitle FROM ChatRoomJpaEntity cr WHERE cr.id = :roomId")
    Optional<String> findTitleById(@Param("roomId") Long roomId);

}
