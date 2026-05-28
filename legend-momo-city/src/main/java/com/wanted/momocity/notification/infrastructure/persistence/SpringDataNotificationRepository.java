package com.wanted.momocity.notification.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataNotificationRepository extends JpaRepository<NotificationJpaEntity, Long> {

    //친구 요청 철회
    void deleteByRefIdAndType(Long refId, String type);

    //메시지 전송
    Optional<NotificationJpaEntity> findByRefIdAndTypeAndUserId_Id(Long refId, String type, Long userId);
}
