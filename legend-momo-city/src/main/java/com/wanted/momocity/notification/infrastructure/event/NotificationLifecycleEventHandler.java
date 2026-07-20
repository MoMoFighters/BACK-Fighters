package com.wanted.momocity.notification.infrastructure.event;

import com.wanted.momocity.community.domain.event.CommentCreatedEvent;
import com.wanted.momocity.community.domain.event.PostLikedEvent;
import com.wanted.momocity.community.domain.event.ReplyCreatedEvent;
import com.wanted.momocity.friend.domain.event.*;
import com.wanted.momocity.lecture.domain.event.LectureStatusChangedEvent;
import com.wanted.momocity.message.domain.event.SendMessagePublishedEvent;
import com.wanted.momocity.notification.application.service.NotificationHandlerService;
import com.wanted.momocity.study.domain.event.StudyInviteEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationLifecycleEventHandler {

    //알림 서비스 하나만 주입
    private final NotificationHandlerService notificationHandlerService;

    //친구 요청 완료 후 발행된 이벤트 처리
    @Async("domainEventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRequestFriend(RequestFriendPublishedEvent event) {
        log.info("[NotificationLifeCycleEventHandler] 친구 요청 이벤트 수신 -> 서비스로 이동");
        //서비스로 던기지
        notificationHandlerService.createAndSaveFriendRequestNotification(
                event.toUserId(), //userId에 넣기(알림 받는 사람)
                event.fromUserNickname(),
                event.fromUserId() //ref_id 용도(알림 발생시킨 사람)
        );
    }

    //친구 요청 철회 완료 후 발행된 이벤트 처리
    @Async("domainEventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCancelRequestFriend(CancelRequestFriendPublishedEvent event) {
        log.info("[NotificationLifecycleEventHandler] 친구 요청 철회 이벤트 수신 -> 알림 서비스로 이동");

        //주입받은 알림 서비스로 토스
        notificationHandlerService.deleteRequestFriendNotification(
                event.fromUserId(), //refId 요청자
                event.toUserId() //userId 대상자
        );
    }

    //친구 요청 수락 완료 후 발행된 이벤트 처리 (요청한 사람에게 친구 완료 알림 저장)
    @Async("domainEventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAcceptRequestFriend(AcceptRequestFriendPublishedEvent event) {
        log.info("[NotificationLifecycleEventHandler] 친구 요청 수락 이벤트 수신 -> 알림 서비스로 이동");

        notificationHandlerService.createAndSaveFriendAcceptNotification(
                event.acceptorUserId(), //수락한 사람
                event.acceptorNickname(), //문구에 들어갈 수락자 닉네임
                event.fromUserId() //refId용
        );

    }

    //친구 요청 거절(알림 읽음 처리)
    @Async("domainEventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) // 거절 커밋 성공 후
    public void handleFriendReject(RejectRequestFriendPublishedEvent event) {
        // 1. 거절한 유저(event.userId)에게 쌓여있던 'FRIEND_REQUEST' 알림 행을 한방에 삭제하거나 읽음 처리
        // 이미 뚫려있는 deleteByRefIdAndUserId_IdAndType 또는 벌크 쿼리 활용!
        notificationHandlerService.readRequestFriendNotification(
                event.userId(), //거절자
                event.fromUserId(),
                event.refId()); //요청자

    }

    //메시지 전송
    @Async("domainEventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSendMessage(SendMessagePublishedEvent event) {
        log.info("[NotificationLifecycleEventHandler] 메시지 전송 -> 알림 서비스로 이동");

        //다대다인 경우 수신자가 null
        Long receiverId = (event.receiverId() != null) ? event.receiverId() : null;

        notificationHandlerService.sendMessageNotification(
                event.roomId(), //refId용
                event.roomTitle(), //다대다를 위한 채팅방 이름
                event.senderId(), //보낸 사람
                event.senderNickname(), //문구에 들어갈 보낸 사람 닉네임
                event.receiverId(),
                event.createdAt() //날짜 업데이트용
        );
    }

    //강사-학생 자동 친구 학생 쪽 알림 행 추가
    @Async("domainEventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAutoFriend(TeacherStudentAutoFriendPublishedEvent event) {
        log.info("[NotificationLifecycleEventHandler] 강사-학생 자동 친구 행 추가 이벤트 수신 -> 알림 서비스로 이동");

        notificationHandlerService.autoFriendNotification(
                event.fromUserId(), //학생 아이디(notification 테이블의 userId: 알림 받을 사람)
                event.toUserId(), //강사 아이디(refId)
                event.teacherName(), //강사 이름
                event.teacherNickname() //강사 닉네임
        );
    }

    //방명록 작성 알림
    @Async("domainEventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleGuestBook(RegisterGuestBookPublishedEvent event) {
        log.info("[NotificationLifecycleEventHandler] 방명록 작성 알림 행 추가 이벤트 수신 -> 알림 서비스로 이동");

        notificationHandlerService.guestBookNotification(
                event.bookId(),
                event.writerId(),
                event.ownerId(),
                event.writerNickname(),
                event.now()
        );
    }

    // 커뮤니티
    //게시글 좋아요
    @Async("domainEventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePostLiked(PostLikedEvent event) {
        log.info("[NotificationLifecycleEventHandler] 게시글 좋아요 알림 행 추가 이벤트 수신 -> 알림 서비스로 이동");

        notificationHandlerService.createPostLikedNotification(
                event.postOwnerId(), //게시글 주인 아이디
                event.likedUserName(), //좋아요 누른 주체 닉네임
                event.postId(), //게시글 아이디
                event.likeUserId() //좋아요 누른 주제 아이디
        );
    }

    //댓글
    @Async("domainEventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommentCreated(CommentCreatedEvent event) {
        log.info("[NotificationLifecycleEventHandler] 게시글 댓글 알림 행 추가 이벤트 수신 -> 알림 서비스로 이동");

        notificationHandlerService.createCommentNotification(
                event.postOwnerId(), //게시글 주인 아이디
                event.commentUserName(), //댓글 작성자 닉네임
                event.postId(), //게시글 아이디
                event.commentUserId() //댓글 작성자 아이디
        );
    }

    //대댓글(게시글 작성자, 대댓글의 부모 댓글 작성자)
    @Async("domainEventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReplyCreated(ReplyCreatedEvent event) {
        log.info("[NotificationLifecycleEventHandler] 게시글 대댓글 알림 행 추가 이벤트 수신 -> 알림 서비스로 이동");

        notificationHandlerService.createReplyNotification(
                event.parentCommentOwnerId(), //부모 댓글 주인 아이디
                event.postOwnerId(), //게시글 주인 아이디
                event.replyUserName(), //대댓글 작성자 닉네임
                event.postId(), //게시글 아이디
                event.replyUserId() //대댓글 작성자 아이디
        );
    }

    //강의 승인/거절 알림
    @Async("domainEventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleLectureApproval(LectureStatusChangedEvent event) {
        log.info("[NotificationLifecycleEventHandler] 강의 알림 행 추가 이벤트 수신 -> 알림 서비스로 이동");

        notificationHandlerService.lectureApprovalNotification(
                event.lectureId(), //강의 아이디
                event.teacherId(), //강사 아이디
                event.adminId(), //관리자 아이디
                event.lectureTitle(), //강의명
                event.lectureStatus(), //승인/거절 여부
                LocalDateTime.from(event.occurredAt()) //승인/거절 날짜
        );
    }

    //열품타 초대 알림
    @Async("domainEventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleStudyInvite(StudyInviteEvent event) {
        log.info("[NotificationLifecycleEventHandler] 그룹 스터티 초대 알림 행 추가 이벤트 수신 -> 알림 서비스로 이동");

        notificationHandlerService.studyInviteNotification(
                event.inviterNickname(), // 초대자 닉네임
                event.invitedUserId(),   // 초대 대상자 아이디
                event.roomId(),          // 그룹방 아이디
                event.invitedAt()        // 초대 시각
        );
    }
}
