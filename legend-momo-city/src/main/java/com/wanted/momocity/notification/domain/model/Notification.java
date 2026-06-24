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

    //친구 요청
    //순수한 도메인 모델 안에서 알림 객체 생성 비즈니스를 정의
    public static Notification createFriendRequest(Long userId, String message, Long refId) {
        return new Notification(null, userId, "FRIEND_REQUEST", refId, message, false);
    }

    //친구 요청 수락
    public static Notification createFriendAccept(Long acceptorUserId, String message, Long fromUserId) {
        return new Notification(
                null,
                fromUserId,
                "FRIEND_REQUEST",
                acceptorUserId,
                message,
                false
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
                null
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
                false
        );
    }

    // 게시글 좋아요 알림
    public static Notification createPostLiked(Long postOwnerId, String likerName, Long postId) {
        String message = String.format("%s님이 회원님의 게시글을 좋아합니다.", likerName);
        return new Notification(null, postOwnerId, "POST", postId, message);
    }

    // 댓글 알림
    public static Notification createComment(Long postOwnerId, String commenterName, Long postId) {
        String message = String.format("%s님이 회원님의 게시글에 댓글을 달았습니다.", commenterName);
        return new Notification(null, postOwnerId, "POST", postId, message);
    }

    // 대댓글 알림 (댓글 작성자)
    public static Notification createReplyToCommenter(Long commentOwnerId, String replierName, Long postId) {
        String message = String.format("%s님이 회원님의 댓글에 답글을 달았습니다.", replierName);
        return new Notification(null, commentOwnerId, "POST", postId, message);
    }

    // 대댓글 알림 (게시글 작성자)
    public static Notification createReplyToPostOwner(Long postOwnerId, String replierName, Long postId) {
        String message = String.format("%s님이 회원님의 게시글에 답글을 달았습니다.", replierName);
        return new Notification(null, postOwnerId, "POST", postId, message);
    }

    // Calendar Todo 알림
    public static Notification createTodoReminder(Long userId, String title) {
        String message = String.format("오늘 할 일 [%s] 을 완료해주세요!", title);
        return new Notification(null, userId, "CALENDAR", null, message);
    }

    // Calendar Memo 알림
    public static Notification createMemoReminder(Long userId, String title) {
        String message = String.format("오늘 일정 [%s] 이 있습니다!", title);
        return new Notification(null, userId, "CALENDAR", null, message);
    }
}
