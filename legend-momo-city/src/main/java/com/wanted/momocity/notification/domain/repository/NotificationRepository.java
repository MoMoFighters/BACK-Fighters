package com.wanted.momocity.notification.domain.repository;

import com.wanted.momocity.message.infrastructure.persistence.MessageReadJpaEntity;
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

    //휴대폰 속 앱별 알림 개수(캘린더, 커뮤니티, 친구)
    long countByUserIdAndType(Long userId, String type);

    //휴대폰 속 앱별 알림 개수(메시지)
    long countTotalUnreadMessages(Long userId);

    //알림 읽기 - 요청온 알림이 notification 테이블에 존재하는지.
    List<NotificationJpaEntity> findAllByIdIn(List<Long> targetId);

    //알림 읽기 - 메시지 알림의 refId(roomId)에 로그인 유저가 속하는지 검증
    List<MessageReadJpaEntity> findMessageReadsByRoomIds(List<Long> messageRoomIds);

    //알림 읽기 - 일반 알림 읽음 상태 저장
    void saveAll(List<NotificationJpaEntity> generalNotisToUpdate);

    //알림 읽기 - 메시지 알림 읽음 상태 저장
    void bulkMarkMessageNotificationsAsRead(List<Long> messageRoomIds, Long userId);

    //알림 읽음 상태 빠른 저장(읽음 개수 웹소켓)
    void fastSaveChanges();

}
