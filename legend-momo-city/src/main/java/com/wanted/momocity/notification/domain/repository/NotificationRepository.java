package com.wanted.momocity.notification.domain.repository;

import com.wanted.momocity.notification.domain.model.Notification;
import com.wanted.momocity.notification.infrastructure.persistence.NotificationJpaEntity;

public interface NotificationRepository {
    //친구 요청 알림
    Notification save(Notification notification);

    //친구 요청 철회
    void deleteByRefIdAndType(Long refId, String type);
}
