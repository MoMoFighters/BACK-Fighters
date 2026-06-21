package com.wanted.momocity.notification.application.service;

import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.momocity.message.application.policy.MessageEligibilityPolicy;
import com.wanted.momocity.notification.domain.model.Notification;
import com.wanted.momocity.notification.infrastructure.persistence.NotificationJpaEntity;
import com.wanted.momocity.notification.domain.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationHandlerService {

    private final NotificationRepository notificationRepository;

    /**
     * 친구 요청 알림 생성 및 저장 비즈니스 로직
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
    }

    //친구 요청 철회 시 친구 요청으로 들어간 notification 행 삭제
    public void deleteRequestFriendNotification(Long fromUserId, Long toUserId) {
        log.info("[NotificationHandlerService] 친구 요청 철회로 인한 알림 삭제 시도 - 철회 요청자: {}", fromUserId);

        //"FRIEND_REQUEST" 타입이면서 refId가 일치하면 삭제
        notificationRepository.deleteByRefIdAndUserId_IdAndType(fromUserId, toUserId, "FRIEND_REQUEST");
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
    }

    //메시지 전송
    public void sendMessageNotification(Long roomId, String senderNickname, Long senderId, Long receiverId, LocalDateTime createdAt) {
        log.info("[NotificationHandlerService] 메시지 전송으로 인한 알림 처리 - 방ID(refId): {}", roomId);

        // 나와의 채팅 확인
        if (senderId.equals(receiverId)) {
            log.info("[NotificationHandlerService] 나와의 채팅방 메시지이므로 알림 생성을 건너뜀");
            return;
        }

        //방 번호와 타입, senderId으로 기존 알림이 이미 존재하는 지 확인
        Optional<Notification> existingNotificationOpt = notificationRepository.findByRefIdAndTypeAndUserId_Id(roomId, "MESSAGE", senderId);

        String message = String.format("%s님이 메시지를 보냈습니다.", senderNickname);

        //기존 알림이 존재하는 경우 ->시간만 업데이트, 읽지 않음 처리
        if (existingNotificationOpt.isPresent()) {
            log.info("[NotificationHandlerService] 기존 알림 존재 -> 시간 업데이트 및 읽지 않음 상태로 변경");
            Notification existingNotification = existingNotificationOpt.get();

            Notification updatedNotification = new Notification(
                    existingNotification.getId(),
                    existingNotification.getUserId(), // receiverId가 유지됨
                    existingNotification.getType(),
                    existingNotification.getRefId(),
                    message
                    //추후 isRead 생기면 주석 해제
//                    false
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

    // 게시글 좋아요 알림
    public void createPostLikedNotification(Long postOwnerId, String likerName, Long postId) {
        Notification notification = Notification.createPostLiked(postOwnerId, likerName, postId);
        notificationRepository.save(notification);
        log.info("[NotificationHandlerService] 좋아요 알림 저장 완료 | postOwnerId={}", postOwnerId);
    }

    // 댓글 알림
    public void createCommentNotification(Long postOwnerId, String commenterName, Long postId) {
        Notification notification = Notification.createComment(postOwnerId, commenterName, postId);
        notificationRepository.save(notification);
        log.info("[NotificationHandlerService] 댓글 알림 저장 완료 | postOwnerId={}", postOwnerId);
    }

    // 대댓글 알림
    // 댓글 작성자 + 게시글 작성자 둘 다 알림
    // 단, 댓글 작성자 = 게시글 작성자면 중복 알림 제외
    public void createReplyNotification(
            Long commentOwnerId, Long postOwnerId, String replierName, Long postId) {
        notificationRepository.save(
                Notification.createReplyToCommenter(commentOwnerId, replierName, postId));

        if (!commentOwnerId.equals(postOwnerId)) {
            notificationRepository.save(
                    Notification.createReplyToPostOwner(postOwnerId, replierName, postId));
        }
        log.info("[NotificationHandlerService] 대댓글 알림 저장 완료 | postId={}", postId);
    }

    // Calendar Todo 알림
    public void createTodoReminderNotification(Long userId, String title) {
        Notification notification = Notification.createTodoReminder(userId, title);
        notificationRepository.save(notification);
        log.info("[NotificationHandlerService] Todo 알림 저장 완료 | userId={}", userId);
    }

    // Calendar Memo 알림
    public void createMemoReminderNotification(Long userId, String title) {
        Notification notification = Notification.createMemoReminder(userId, title);
        notificationRepository.save(notification);
        log.info("[NotificationHandlerService] Memo 알림 저장 완료 | userId={}", userId);
    }

}
