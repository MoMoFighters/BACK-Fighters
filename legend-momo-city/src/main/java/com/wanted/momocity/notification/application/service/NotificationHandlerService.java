package com.wanted.momocity.notification.application.service;

import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.momocity.message.application.policy.MessageEligibilityPolicy;
import com.wanted.momocity.notification.application.query.GetMainTotalCountsQuery;
import com.wanted.momocity.notification.application.query.GetNotificationQuery;
import com.wanted.momocity.notification.application.usecase.NotificationQueryUseCase;
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
    //웹소켓으로 실시간 알림 전송 받는 거 처리
    private final NotificationQueryUseCase notificationQueryUseCase;

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
        notificationQueryUseCase.getMainTotalCountsQueryHandle(new GetMainTotalCountsQuery(toUserId));
        notificationQueryUseCase.getNotificationQueryHandle(new GetNotificationQuery(toUserId));
        log.info("[알림 핸들러 -> 쿼리 연동] 온라인 유저 {}번에게 실시간 알림 웹소켓 전송 완료", toUserId);
    }

    //친구 요청 철회 시 친구 요청으로 들어간 notification 행 삭제
    public void deleteRequestFriendNotification(Long fromUserId, Long toUserId) {
        log.info("[NotificationHandlerService] 친구 요청 철회로 인한 알림 삭제 시도 - 철회 요청자: {}", fromUserId);

        //"FRIEND_REQUEST" 타입이면서 refId가 일치하면 삭제
        notificationRepository.deleteByRefIdAndUserId_IdAndType(fromUserId, toUserId, "FRIEND_REQUEST");

        // 현재 화면에 붙어있는 유저라면 즉시 가공해서 푸시!
        notificationQueryUseCase.getMainTotalCountsQueryHandle(new GetMainTotalCountsQuery(toUserId));
        notificationQueryUseCase.getNotificationQueryHandle(new GetNotificationQuery(toUserId));
        log.info("[알림 핸들러 -> 쿼리 연동] 온라인 유저 {}번에게 실시간 알림 웹소켓 전송 완료", toUserId);
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

        // 현재 화면에 붙어있는 유저라면 즉시 가공해서 푸시!
        notificationQueryUseCase.getMainTotalCountsQueryHandle(new GetMainTotalCountsQuery(fromUserId));
        notificationQueryUseCase.getNotificationQueryHandle(new GetNotificationQuery(fromUserId));
        log.info("[알림 핸들러 -> 쿼리 연동] 온라인 유저 {}번에게 실시간 알림 웹소켓 전송 완료", fromUserId);
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
                    null //notification 관련 알림은 message_read에서 처리하므로 null 처리
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

        // ⭕ 누락된 실시간 푸시 추가 (학생 쪽 아이디인 fromUserId에게 푸시)
        notificationQueryUseCase.getMainTotalCountsQueryHandle(new GetMainTotalCountsQuery(fromUserId));
        notificationQueryUseCase.getNotificationQueryHandle(new GetNotificationQuery(fromUserId));
        log.info("[알림 핸들러 -> 쿼리 연동] 온라인 유저 {}번(학생)에게 실시간 자동 친구 알림 웹소켓 전송 완료", fromUserId);
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
        notificationQueryUseCase.getMainTotalCountsQueryHandle(new GetMainTotalCountsQuery(ownerId));
        notificationQueryUseCase.getNotificationQueryHandle(new GetNotificationQuery(ownerId));
        log.info("[알림 핸들러 -> 쿼리 연동] 온라인 유저 {}번(도시주인)에게 실시간 방명록 알림 웹소켓 전송 성공", ownerId);
    }
}
