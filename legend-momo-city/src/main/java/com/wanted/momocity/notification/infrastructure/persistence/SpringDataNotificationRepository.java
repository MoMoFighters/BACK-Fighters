package com.wanted.momocity.notification.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataNotificationRepository extends JpaRepository<NotificationJpaEntity, Long> {

    //친구 요청 철회
    void deleteByRefIdAndType(Long refId, String type);
}
