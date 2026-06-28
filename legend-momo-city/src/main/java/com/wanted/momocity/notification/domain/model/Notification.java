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
    //v2
    private final Boolean isRead;
    private final LocalDateTime createdAt;

    //친구 요청
    //순수한 도메인 모델 안에서 알림 객체 생성 비즈니스를 정의
    public static Notification createFriendRequest(Long userId, String message, Long refId) {
        return new Notification(null, userId, "FRIEND_REQUEST", refId, message, false, LocalDateTime.now());
    }

    //친구 요청 수락
    public static Notification createFriendAccept(Long acceptorUserId, String message, Long fromUserId) {
        return new Notification(
                null,
                fromUserId,
                "FRIEND_REQUEST",
                acceptorUserId,
                message,
                false,
                LocalDateTime.now()
        );
    }

    //메시지 전송
    public static Notification createMessageNotification(Long senderId, String type, Long roomId, String message) {
        return new Notification(
                null,
                senderId,
                type,
                roomId,
                message,
                //isRead 생기면 주석 해제
                null,
                LocalDateTime.now()
        );
    }

    //강사-학생 자동 친구 알림 행 생성(알림 받을 사람 userId: 학생, refId: 강사)
    public static Notification createAutoFriend(Long fromUserId, String message, Long toUserId) {
        return new Notification(
                null,
                fromUserId,
                "FRIEND_REQUEST",
                toUserId,
                message,
                false,
                LocalDateTime.now()

        );
    }

    //방명록 알림 생성
    public static Notification createGuestBook(Long ownerId, String message, Long bookId, LocalDateTime now) {
        return new Notification(
                null,
                ownerId,
                "GUESTBOOK",
                bookId,
                message,
                false,
                now
        );
    }

    //게시글 좋아요 알림
    public static Notification likePost(Long postOwnerId, String message, Long postId) {
        return new Notification(
                null,
                postOwnerId,
                "POST",
                postId,
                message,
                false,
                LocalDateTime.now()
        );
    }

    //게시글 댓글 알림
    public static Notification commentPost(Long postOwnerId, String message, Long postId) {
        return new Notification(
                null,
                postOwnerId,
                "POST",
                postId,
                message,
                false,
                LocalDateTime.now()
        );
    }

    //대댓글 알림(부모 댓글의 주인에게 알림)
    public static Notification replyComment(Long parentCommentOwnerId, String replyMessage, Long postId) {
        return new Notification(
                null,
                parentCommentOwnerId,
                "POST",
                postId,
                replyMessage,
                false,
                LocalDateTime.now()
        );
    }

    //대댓글 알림(게시글 주인에게 알림)
    public static Notification postComment(Long postOwnerId, String postMessage, Long postId) {
        return new Notification(
                null,
                postOwnerId,
                "POST",
                postId,
                postMessage,
                false,
                LocalDateTime.now()
        );
    }

    //캘린더 투두 알림
    public static Notification todoCalendar(Long userId, String message, Long todoId) {
        return new Notification(
                null,
                userId,
                "CALENDAR",
                todoId,
                message,
                false,
                LocalDateTime.now()
        );
    }

    //캘린더 메모 알림
    public static Notification memoCalendar(Long userId, String message, Long memoId) {
        return new Notification(
                null,
                userId,
                "CALENDAR",
                memoId,
                message,
                false,
                LocalDateTime.now()
        );
    }
}
