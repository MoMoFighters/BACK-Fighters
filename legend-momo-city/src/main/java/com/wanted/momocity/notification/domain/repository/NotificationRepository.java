package com.wanted.momocity.notification.domain.repository;

import com.wanted.momocity.notification.domain.model.Notification;
import com.wanted.momocity.notification.infrastructure.persistence.NotificationJpaEntity;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository {
    //친구 요청 알림
    Notification save(Notification notification);

    //친구 요청 철회
    void deleteByRefIdAndUserId_IdAndType(Long refId, Long userId, String type);

    //메시지 전송 - 기존 알림 존재 여부 확인(채팅방 번호, 타입, 보낸 사람 아이디)
    Optional<Notification> findByRefIdAndTypeAndUserId_Id(Long roomId, String message, Long senderId);

    //알림 목록
    List<Object[]> findAllByUserId(Long aLong);

    //알림 목록 - 방 이름
    Optional<String> findRoomTitleById(Long roomId);

    //(메인 페이지 종)안읽은 전체 알림 개수(메시지 알림 제외)
    long countUnreadGeneral(Long userId);

    //(메인 페이지 종)안읽고 삭제하지 않은 메시지의 채팅방 개수
    long countUnreadMessageRooms(Long userId);
}
