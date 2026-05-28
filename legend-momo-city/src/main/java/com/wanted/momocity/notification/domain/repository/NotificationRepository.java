package com.wanted.momocity.notification.domain.repository;

import com.wanted.momocity.notification.domain.model.Notification;
import com.wanted.momocity.notification.infrastructure.persistence.NotificationJpaEntity;

import java.util.Optional;

public interface NotificationRepository {
    //친구 요청 알림
    Notification save(Notification notification);

    //친구 요청 철회
    void deleteByRefIdAndType(Long refId, String type);

    //메시지 전송 - 기존 알림 존재 여부 확인(채팅방 번호, 타입, 보낸 사람 아이디)
    Optional<Notification> findByRefIdAndTypeAndUserId_Id(Long roomId, String message, Long senderId);
}
