package com.wanted.momocity.notification.application.service;

import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.momocity.lecture.domain.model.LectureStatus;
import com.wanted.momocity.message.application.policy.MessageEligibilityPolicy;
import com.wanted.momocity.notification.application.query.GetMainTotalCountsQuery;
import com.wanted.momocity.notification.application.query.GetNotificationQuery;
import com.wanted.momocity.notification.application.query.GetPhoneAppCountsQuery;
import com.wanted.momocity.notification.application.usecase.NotificationQueryUseCase;
import com.wanted.momocity.notification.domain.model.Notification;
import com.wanted.momocity.notification.infrastructure.event.NotificationCreatedPublishedEvent;
import com.wanted.momocity.notification.infrastructure.persistence.NotificationJpaEntity;
import com.wanted.momocity.notification.domain.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.wanted.momocity.lecture.domain.model.LectureStatus.ACTIVE;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationHandlerService {

    private final NotificationRepository notificationRepository;
    //웹소켓으로 실시간 알림 전송 받는 거 처리
    private final NotificationQueryUseCase notificationQueryUseCase;
    //이벤트 발행
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 친구 요청 알림 생성 및 저장 비즈니스 로직
     * 수신자: userId(알림 받을 사람)
     */
    public void createAndSaveFriendRequestNotification(Long toUserId, String fromUserNickname, Long fromUserId) {
        log.info("[NotificationHandlerService] 알림 비즈니스 로직 시작 - 대상자 ID: {}", toUserId);

        //알림 메시지 조립
        String message = String.format("%s님이 친구 요청을 보냈습니다.", fromUserNickname);

        //순수한 도메인 모델 직접 탄생시킴
        Notification newNotification = Notification.createFriendRequest(toUserId, message, fromUserId);

        //도메인 규격 리포지토리를 통해 저장 수행
        Notification saved = notificationRepository.save(newNotification);
        log.info("[NotificationHandlerService] notification 테이블 행 추가 완료 - 생성된 알림ID: {}", saved.getId());

        // 현재 화면에 붙어있는 유저라면 즉시 가공해서 푸시!
        eventPublisher.publishEvent(new NotificationCreatedPublishedEvent(toUserId, "ALL"));
        log.info("[알림 핸들러 -> 쿼리 연동] 온라인 유저 {}번에게 실시간 알림 웹소켓 전송 완료", toUserId);
    }

    //친구 요청 철회 시 친구 요청으로 들어간 notification 행 삭제
    public void deleteRequestFriendNotification(Long fromUserId, Long toUserId) {
        log.info("[NotificationHandlerService] 친구 요청 철회로 인한 알림 삭제 시도 - 철회 요청자: {}", fromUserId);

        //"FRIEND_REQUEST" 타입이면서 refId가 일치하면 삭제
        notificationRepository.deleteByRefIdAndUserId_IdAndType(fromUserId, toUserId, "FRIEND_REQUEST");

        log.info("[NotificationHandlerService] notification 테이블 행 삭제 완료");

        // 🎯 [개선 핵심] 메인 흐름을 가볍게 만들기 위해, 이미 구축해 둔 비동기 이벤트 리스너를 재활용합니다!
        // 상대방(toUserId) 화면의 알림 목록 및 폰 카운트가 싹 갱신되어야 하므로 "ALL" 타입을 던집니다.
        eventPublisher.publishEvent(new NotificationCreatedPublishedEvent(toUserId, "ALL"));
        log.info("[알림 핸들러 -> 이벤트 발행] 온라인 유저 {}번 화면 갱신용 비동기 이벤트 전달 완료", toUserId);

    }

    //친구 요청 수락(상태 변경 SENT -> FRIEND)
    public void createAndSaveFriendAcceptNotification(Long acceptorUserId, String acceptorNickname, Long fromUserId) {
        log.info("[NotificationHandlerService] 친구 수락 알림 처리 시작 - 행위 유발자ID: {}, 요청자ID: {}", acceptorUserId, fromUserId);

        //메시지 조립
        String message = String.format("%s님과 친구가 되었습니다. 교류를 시작해보세요!", acceptorNickname);

        //순수한 도메인 모델 생성
        Notification newNotification = Notification.createFriendAccept(acceptorUserId, message, fromUserId);

        //레포지토리를 통해 알림 저장
        Notification saved = notificationRepository.save(newNotification);
        log.info("[NotificationHandlerService] 수락 알림 생성 완료 - 생성된 알림ID: {}", saved.getId());

        // 🎯 [최적화 1] 직접 무겁게 쿼리 핸들러들을 호출하던 코드를 전면 제거합니다!
        // 🎯 [최적화 2] 미리 구축해두신 웹소켓 이벤트 주머니를 발행하여 비동기 스레드로 책임을 격리합니다.
        // 친구 알림은 휴대폰 화면 갱신이 필요하므로 "ALL" 타입을 실어 보냅니다.
        eventPublisher.publishEvent(new NotificationCreatedPublishedEvent(fromUserId, "ALL"));
        log.info("[알림 핸들러 -> 최적화] 웹소켓 갱신 이벤트를 비동기 리스너로 발행 완료. 유저ID: {}", fromUserId);
    }

    //친구 요청 거절(알림 읽음 처리)
    public void readRequestFriendNotification(Long userId, Long fromUserId, Long refId) {
        // 1. 거절한 유저(event.userId)에게 쌓여있던 'FRIEND_REQUEST' 알림 행을 한방에 삭제하거나 읽음 처리
        // 이미 뚫려있는 deleteByRefIdAndUserId_IdAndType 또는 벌크 쿼리 활용!
        notificationRepository.bulkMarkAsReadByRefIdAndUserIdAndType(refId, userId, "FRIEND_REQUEST");

        // 2. 알림이 지워졌으니 내 폰 화면의 실시간 웹소켓 배지 카운트도 갱신하라고 다시 알림 이벤트 퐁당 던지기!
        eventPublisher.publishEvent(new NotificationCreatedPublishedEvent(userId, "ALL"));
    }

    //메시지 전송
    public void sendMessageNotification(Long roomId, String roomTitle, Long senderId, String senderNickname, Long receiverId, LocalDateTime createdAt) {
        log.info("[NotificationHandlerService] 메시지 전송으로 인한 알림 처리 - 방ID(refId): {}", roomId);

        // 나와의 채팅 확인
        if (senderId.equals(receiverId)) {
            log.info("[NotificationHandlerService] 나와의 채팅방 메시지이므로 알림 생성을 건너뜀");
            return;
        }

        //방 번호와 타입, senderId으로 기존 알림이 이미 존재하는 지 확인
        Optional<Notification> existingNotificationOpt = notificationRepository.findByRefIdAndTypeAndUserId_Id(roomId, "MESSAGE", senderId);

        String message = "";
        //일대일 채팅인 경우
        if (roomTitle == null || roomTitle.isEmpty()) {
            message = String.format("%s님이 메시지를 보냈습니다.", senderNickname);
        } else {
            message = String.format("[%s] %s님이 메시지를 보냈습니다.", roomTitle, senderNickname);
        }

        //기존 알림이 존재하는 경우 ->시간만 업데이트, 읽지 않음 처리
        if (existingNotificationOpt.isPresent()) {
            log.info("[NotificationHandlerService] 기존 알림 존재 -> 시간 업데이트 및 읽지 않음 상태로 변경");
            Notification existingNotification = existingNotificationOpt.get();

            Notification updatedNotification = new Notification(
                    existingNotification.getId(),
                    existingNotification.getUserId(), // receiverId가 유지됨
                    existingNotification.getType(),
                    existingNotification.getRefId(),
                    message,
                    //추후 isRead 생기면 주석 해제
                    null, //notification 관련 알림은 message_read에서 처리하므로 null 처리
                    createdAt
            );

            notificationRepository.save(updatedNotification);
            return;
        }

        //기존 알림이 없는 경우 -> 새로 행 추가
        log.info("[NotificationHandlerService] 기존 알림 없음 -> 새로운 알림 행 추가");

        Notification newNotification = Notification.createMessageNotification(
                senderId,
                "MESSAGE",
                roomId,
                message
        );

        notificationRepository.save(newNotification);

    }

    //강사-학생 자동 친구 행 추가 시 학생 쪽 알림
    public void autoFriendNotification(Long fromUserId, Long toUserId, String teacherName, String teacherNickname) {
        log.info("[NotificationHandlerService] 친구 수락 알림 처리 시작 - 행위 유발자ID: {}", fromUserId);

        String message;
        //강사 닉네임이 없는 경우 확인
        if (teacherNickname == null || teacherNickname.isEmpty()) {
            //메시지 조립
            message = String.format("%s강사님과 자동으로 친구가 되었습니다. 질문을 시작해보세요!", teacherName);
        } else {
            message = String.format("%s강사님과 자동으로 친구가 되었습니다. 질문을 시작해보세요!", teacherNickname + "(" + teacherName + ")");
        }

        //순수한 도메인 모델 생성
        Notification newNotification = Notification.createAutoFriend(fromUserId, message, toUserId);

        //레포지토리를 통해 알림 저장
        Notification saved = notificationRepository.save(newNotification);
        log.info("[NotificationHandlerService] 자동 친구 알림 생성 완료 - 생성된 알림ID: {}", saved.getId());

        // 🎯 [최적화 핵심] 메인 스레드 병목 방지! 무거운 웹소켓 재조회는 비동기 리스너에게 이벤트를 던져 위임합니다.
        // 수신 대상자인 학생(fromUserId)의 폰 카운트까지 다 채워야 하므로 "ALL" 타입을 발행합니다.
        eventPublisher.publishEvent(new NotificationCreatedPublishedEvent(fromUserId, "ALL"));
        log.info("[알림 핸들러 -> 이벤트 발행] 학생 유저 {}번 화면 갱신용 비동기 이벤트 전달 완료", fromUserId);
    }

    //방명록 작성
    public void guestBookNotification(Long bookId, Long writerId, Long ownerId, String writerNickname, LocalDateTime now) {
        log.info("[NotificationHandlerService] 방명록 작성 알림 처리 시작 - 방명록ID(refId): {}, 작성자: {}, 도시주인: {}", bookId, writerId, ownerId);

        // 1. 자기 자신의 도시에 방명록을 남긴 경우 알림 생성을 건너뜁니다.
        if (writerId.equals(ownerId)) {
            log.info("[NotificationHandlerService] 본인 도시에 작성한 방명록이므로 알림 생성을 건너뜀");
            return;
        }

        // 2. 명세서 스펙에 맞춘 알림 텍스트 조립 ("{nickname}님이 회원님의 도시에 방명록을 남겼습니다.")
        String message = String.format("%s님이 회원님의 도시에 방명록을 남겼습니다.", writerNickname);

        // 3. 순수한 도메인 모델(Aggregate) 생성
        // (Notification 도메인 내부에 관련 팩토리 메서드가 정의되어 있다고 가정하거나 일반 create 사용)
        // 알림을 받는 주인은 ownerId이고, 링크(참조)되는 ID는 생성된 방명록 PK(bookId)입니다.
        Notification newNotification = Notification.createGuestBook(ownerId, message, bookId, now);

        // 4. 레포지토리를 통해 알림 테이블에 적재
        Notification saved = notificationRepository.save(newNotification);
        log.info("[NotificationHandlerService] 방명록 알림 생성 완료 - 생성된 알림ID: {}", saved.getId());

        // 5. 현재 화면을 보고 있을 도시 주인을 위해 즉시 실시간 웹소켓 푸시 연동!
        // ⭕ 일관성을 맞추기 위해 불필요한 try-catch 제거하고 다이렉트 트리거 실행
        eventPublisher.publishEvent(new NotificationCreatedPublishedEvent(ownerId, "NOTPHONE"));
        log.info("[알림 핸들러 -> 쿼리 연동] 온라인 유저 {}번(도시주인)에게 실시간 방명록 알림 웹소켓 전송 성공", ownerId);
    }

    //게시글 좋아요 알림
    public void createPostLikedNotification(Long postOwnerId, String likedUserName, Long postId, Long likeUserId) {
        log.info("[NotificationHandlerService] 게시글 좋아요 알림 처리 시작 - 게시글ID(refId): {}, 좋아요주체: {}, 게시글주인: {}", postId, likeUserId, postOwnerId);

        // 1. 자기 자신의 게시글에 좋아요 누른 경우 알림 생성을 건너뜁니다.
        if (likeUserId.equals(postOwnerId)) {
            log.info("[NotificationHandlerService] 본인 게시글에 누른 좋아요이므로 알림 생성을 건너뜀");
            return;
        }

        String message = String.format("%s님이 회원님의 게시글을 좋아합니다.", likedUserName);

        Notification newNotification = Notification.likePost(postOwnerId, message, postId);

        // 4. 레포지토리를 통해 알림 테이블에 적재
        Notification saved = notificationRepository.save(newNotification);
        log.info("[NotificationHandlerService] 게시글 좋아요 알림 생성 완료 - 생성된 알림ID: {}", saved.getId());

        eventPublisher.publishEvent(new NotificationCreatedPublishedEvent(postOwnerId, "ALL"));
        log.info("[알림 핸들러 -> 쿼리 연동] 온라인 유저 {}번(게시글주인)에게 실시간 게시글 좋아요 알림 웹소켓 전송 성공", postOwnerId);
    }

    //게시글 댓글 -> 게시글 주인 알림
    public void createCommentNotification(Long postOwnerId, String commentUserName, Long postId, Long commentUserId) {
        log.info("[NotificationHandlerService] 게시글 댓글 알림 처리 시작 - 게시글ID(refId): {}, 댓글작성자: {}, 게시글주인: {}", postId, commentUserId, postOwnerId);

        // 1. 자기 자신의 게시글에 댓글을 단 경우 알림 생성을 건너뜁니다.
        if (commentUserId.equals(postOwnerId)) {
            log.info("[NotificationHandlerService] 본인 게시글에 작성한 댓글이므로 알림 생성을 건너뜀");
            return;
        }

        String message = String.format("%s님이 회원님의 게시글에 댓글을 달았습니다.", commentUserName);

        Notification newNotification = Notification.commentPost(postOwnerId, message, postId);

        // 4. 레포지토리를 통해 알림 테이블에 적재
        Notification saved = notificationRepository.save(newNotification);
        log.info("[NotificationHandlerService] 게시글 댓글 알림 생성 완료 - 생성된 알림ID: {}", saved.getId());

        eventPublisher.publishEvent(new NotificationCreatedPublishedEvent(postOwnerId, "ALL"));
        log.info("[알림 핸들러 -> 쿼리 연동] 온라인 유저 {}번(게시글주인)에게 실시간 게시글 댓글 알림 웹소켓 전송 성공", postOwnerId);
    }

    //게시글 대댓글 -> 게시글 주인, 대댓글 부모 댓글 작성자
    public void createReplyNotification(Long parentCommentOwnerId, Long postOwnerId, String replyUserName, Long postId, Long replyUserId) {
        // 로그의 commentUserId가 없어서 replyUserId로 수정했습니다.
        log.info("[NotificationHandlerService] 게시글 대댓글 알림 처리 시작 - 게시글ID(refId): {}, 대댓글작성자: {}, 게시글주인: {}, 부모댓글주인: {}",
                postId, replyUserId, postOwnerId, parentCommentOwnerId);

        String replyMessage = String.format("%s님이 회원님의 댓글에 답글을 달았습니다.", replyUserName);
        String postMessage = String.format("%s님이 회원님의 게시글에 답글을 달았습니다.", replyUserName);

        // ----------------------------------------------------
        // 1. 부모 댓글 작성자에게 알림 생성 (조건: 작성자 본인이 아니어야 함)
        // ----------------------------------------------------
        if (!replyUserId.equals(parentCommentOwnerId)) {
            Notification newNotificationReply = Notification.replyComment(parentCommentOwnerId, replyMessage, postId);
            Notification replySaved = notificationRepository.save(newNotificationReply);
            log.info("[NotificationHandlerService] 대댓글 알림 생성 완료 - 알림ID: {}", replySaved.getId());

            // 실시간 웹소켓 전송
            eventPublisher.publishEvent(new NotificationCreatedPublishedEvent(parentCommentOwnerId, "ALL"));
            log.info("[알림 핸들러] 유저 {}번(부모댓글주인)에게 실시간 웹소켓 전송", parentCommentOwnerId);
        }

        // ----------------------------------------------------
        // 2. 게시글 작성자에게 알림 생성
        // 🔥 중복 방지 조건 1: 게시글 주인과 부모 댓글 주인이 같으면 이미 위에서 알림이 갔으므로 건너뜀!
        // 🔥 조건 2: 대댓글 작성자 본인이 아니어야 함
        // ----------------------------------------------------
        if (!postOwnerId.equals(parentCommentOwnerId) && !replyUserId.equals(postOwnerId)) {
            Notification newNotificationPost = Notification.postComment(postOwnerId, postMessage, postId);
            Notification postSaved = notificationRepository.save(newNotificationPost);
            log.info("[NotificationHandlerService] 게시글 알림 생성 완료 - 알림ID: {}", postSaved.getId());

            // 실시간 웹소켓 전송
            eventPublisher.publishEvent(new NotificationCreatedPublishedEvent(postOwnerId, "ALL"));
            log.info("[알림 핸들러] 유저 {}번(게시글주인)에게 실시간 웹소켓 전송", postOwnerId);
        }
    }

    //캘린더 알림
    public void createTodoNotification(Long userId, Long todoId, String title) {
        log.info("[NotificationHandlerService] 캘린더 To-do 알림 처리 시작 - 투두ID(refId): {}", todoId);

        String message = String.format("오늘 할 일 [%s] 을 완료해주세요!", title);

        Notification newNotification = Notification.todoCalendar(userId, message, todoId);

        // 4. 레포지토리를 통해 알림 테이블에 적재
        Notification saved = notificationRepository.save(newNotification);
        log.info("[NotificationHandlerService] 캘린더 To-do 알림 생성 완료 - 생성된 알림ID: {}", saved.getId());

        eventPublisher.publishEvent(new NotificationCreatedPublishedEvent(userId, "ALL"));
        log.info("[알림 핸들러 -> 쿼리 연동] 온라인 유저 {}번(투두주인)에게 실시간 캘린더 To-do 알림 웹소켓 전송 성공", userId);
    }

    //캘린더 메모 알림
    public void createMemoNotification(Long userId, Long memoId, String title) {
        log.info("[NotificationHandlerService] 캘린더 메모 알림 처리 시작 - 메모ID(refId): {}", memoId);

        String message = String.format("오늘 일정 [%s] 이 있습니다!", title);

        Notification newNotification = Notification.memoCalendar(userId, message, memoId);

        // 4. 레포지토리를 통해 알림 테이블에 적재
        Notification saved = notificationRepository.save(newNotification);
        log.info("[NotificationHandlerService] 캘린더 메모 알림 생성 완료 - 생성된 알림ID: {}", saved.getId());

        eventPublisher.publishEvent(new NotificationCreatedPublishedEvent(userId, "ALL"));
        log.info("[알림 핸들러 -> 쿼리 연동] 온라인 유저 {}번(메모주인)에게 실시간 캘린더 메모 알림 웹소켓 전송 성공", userId);
    }

    //강의 승인/거절 알림
    public void lectureApprovalNotification(Long lectureId, Long teacherId, Long adminId, String lectureTitle, LectureStatus lectureStatus, LocalDateTime occurredAt) {
        log.info("[NotificationHandlerService] 강의 승인/거절 알림 처리 시작 - 강의ID(refId): {}, 강사ID:{}", lectureId, teacherId);

        String message;
        if (ACTIVE.equals(lectureStatus)) {
            message = String.format("[%s] 강의가 승인되었습니다.", lectureTitle);
        } else {
            message = String.format("[%s] 강의가 거절되었습니다.", lectureTitle);
        }

        Notification newNotification = Notification.lectureApproval(teacherId, message, lectureId, occurredAt);

        // 4. 레포지토리를 통해 알림 테이블에 적재
        Notification saved = notificationRepository.save(newNotification);
        log.info("[NotificationHandlerService] 강의 승인/거절 알림 생성 완료 - 생성된 알림ID: {}", saved.getId());

        eventPublisher.publishEvent(new NotificationCreatedPublishedEvent(teacherId, "NOTPHONE"));
        log.info("[알림 핸들러 -> 쿼리 연동] 온라인 유저 {}번(강의주인)에게 실시간 강의 승인/거절 알림 웹소켓 전송 성공", teacherId);
    }

}
