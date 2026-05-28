package com.wanted.momocity.notification.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class Notification {
    private final Long id;
    private final Long userId;
    private final String type;
    private final Long refId;
    private final String message;
    //추후
//    private final boolean isRead;

    //친구 요청
    //순수한 도메인 모델 안에서 알림 객체 생성 비즈니스를 정의
    public static Notification createFriendRequest(Long userId, String message, Long refId) {
        return new Notification(null, userId, "FRIEND_REQUEST", refId, message);
    }

    //친구 요청 수락
    public static Notification createFriendAccept(Long triggerUserId, String message, Long friendId) {
        return new Notification(
                null,
                triggerUserId,
                "FRIEND_REQUEST",
                friendId,
                message);
    }

    //메시지 전송
    public static Notification createMessageNotification(Long senderId, String type, Long roomId, String message) {
        return new Notification(
                null,
                senderId,
                type,
                roomId,
                message
                //isRead 생기면 주석 해제
//                false
        );
    }
}
