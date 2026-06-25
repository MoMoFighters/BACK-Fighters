package com.wanted.momocity.notification.infrastructure.persistence;

import com.wanted.momocity.message.infrastructure.persistence.ChatRoomJpaEntity;
import com.wanted.momocity.message.infrastructure.persistence.MessageReadJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationSideMessageReadRepository extends JpaRepository<MessageReadJpaEntity, Long> {
}
