package com.wanted.momocity.notification.application.service;

import com.wanted.momocity.friend.fmexception.FMResourceNotFoundException;
import com.wanted.momocity.message.infrastructure.persistence.MessageReadJpaEntity;
import com.wanted.momocity.notification.application.command.ReadNotificationCommand;
import com.wanted.momocity.notification.application.command.RemoveNotificationCommand;
import com.wanted.momocity.notification.application.policy.NotificationEligibilityPolicy;
import com.wanted.momocity.notification.application.usecase.NotificationCommandUseCase;
import com.wanted.momocity.notification.application.usecase.NotificationQueryUseCase;
import com.wanted.momocity.notification.domain.repository.NotificationRepository;
import com.wanted.momocity.notification.infrastructure.event.NotificationCreatedPublishedEvent;
import com.wanted.momocity.notification.infrastructure.persistence.NotificationJpaEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationCommandService implements NotificationCommandUseCase {

    private final NotificationRepository notificationRepository;
    private final NotificationEligibilityPolicy notificationEligibilityPolicy;
    private final NotificationQueryUseCase notificationQueryUseCase;
    private final ApplicationEventPublisher eventPublisher;

    //알림 읽기
    @Override
    public void readNotificationCommandHandle(ReadNotificationCommand command) {
        // [정책 위임 변경]: 400 빈 값 검증 정책을 최상단에서 호출하여 공회전 방지!
        notificationEligibilityPolicy.validateReadRequest(command.targetId());

        log.info("[ReadNotificationCommandService] 알림 읽음 처리 시작 - 유저ID: {}, 대상 알림개수: {}", command.userId(), command.targetId().size());

        // 2. 중복이 섞여 들어온 targetId 리스트를 여기서 순수 고유 ID 리스트로 변환 (정규화)
        List<Long> uniqueTargetIds = command.targetId().stream().distinct().toList();

        // 1. 요청된 알림 마스터(NotificationJpaEntity)들 한 번에 조회
        List<NotificationJpaEntity> notifications = notificationRepository.findAllByIdIn(uniqueTargetIds);

        // 실패 4 — 404 미존재 검증 (요청한 개수와 DB에서 찾은 개수가 다르면 없는 게 섞여 있음)
        if (notifications.size() != uniqueTargetIds.size()) {
            throw new FMResourceNotFoundException("존재하지 않는 알림이 포함되어 있습니다.");
        }

        // 권한 상태의 기본값을 true로 두고 검증 결과에 따라 스위칭합니다.
        boolean hasGeneralAccess = true;
        boolean hasMsgNotiAccess = true;

        List<NotificationJpaEntity> generalNotisToUpdate = new ArrayList<>();
        List<Long> messageRoomIds = new ArrayList<>();

        // 2. 알림 권한 체크 및 타입 분기
        // 2. 알림별 권한 검증 및 분기
        for (NotificationJpaEntity noti : notifications) {

            // A. 일반 알림 처리
            if (!"MESSAGE".equals(noti.getType())) {
                // 내 알림이 맞는지 유무를 불린으로 판단하여 정책 클래스에 위임
                // 내 알림이 아니라면 일반 알림 권한 플래그를 false로 변경
                if (!noti.getUserId().getId().equals(command.userId())) {
                    hasGeneralAccess = false;
                    break;
                }

                // 아직 안 읽은 일반 알림만 수정 대상으로 수집 (자원 절약)
                if (!noti.getIsRead()) {
                    generalNotisToUpdate.add(noti);
                }
            }
            // B. 메시지 알림 처리 (방 ID만 수집)
            else {
                messageRoomIds.add(noti.getRefId());
            }
        }

        if (!messageRoomIds.isEmpty()) {
            // notification의 refId(방 ID 목록)를 기준으로 관련 message_read 테이블 행들을 전부 조회
            List<MessageReadJpaEntity> roomMessageReads = notificationRepository
                    .findMessageReadsByRoomIdsAndUserId(messageRoomIds, command.userId()); // 유저 아이디 조건 없이 방 기준으로만 조회

            // [수정]: 읽기에서도 권한을 가진 방의 개수를 정확히 집계 (anyMatch 차단)
            long myAuthorizedRoomCount = roomMessageReads.stream()
                    .map(mr -> mr.getRoomId().getId())
                    .distinct()
                    .count();

            // 요청한 고유 방 개수와 조회된 내 방 개수가 '전부 일치'해야 승인
            long requestedRoomCount = messageRoomIds.stream().distinct().count();
            hasMsgNotiAccess = (myAuthorizedRoomCount == requestedRoomCount);
        }

        notificationEligibilityPolicy.readNotification(hasGeneralAccess, hasMsgNotiAccess); // false면 정책에서 403 예외 발생

        // 4. 상태 변경이 필요한 진짜 안 읽은 데이터들만 최종 반영 (자원 낭비 원천 차단)
        if (!generalNotisToUpdate.isEmpty()) {
            notificationRepository.bulkMarkGeneralNotificationsAsRead(generalNotisToUpdate);
        }
        if (!messageRoomIds.isEmpty()) {
            notificationRepository.bulkMarkMessageNotificationsAsRead(messageRoomIds, command.userId());
        }

        notificationRepository.fastSaveChanges();
        log.info("[ReadNotificationCommandService] 읽음 처리 완료 (일반: {}건, 메시지: {}건) -> 실시간 웹소켓 갱신 진행",
                generalNotisToUpdate.size(), messageRoomIds.size());

        eventPublisher.publishEvent(new NotificationCreatedPublishedEvent(command.userId(), "ALL"));
        log.info("[ReadNotificationCommandService] 유저 {}번 화면 실시간 데이터 브로드캐스팅 완료", command.userId());
    }

    //알림 삭제
    @Override
    public void removeNotificationCommandHandle(RemoveNotificationCommand command) {
        // [정책 위임 변경]: 400 빈 값 검증 정책을 최상단에서 호출하여 공회전 방지!
        notificationEligibilityPolicy.validateRemoveRequest(command.targetId());

        log.info("[RemoveNotificationCommandService] 알림 삭제 처리 시작 - 유저ID: {}, 대상 알림개수: {}", command.userId(), command.targetId().size());

        // 2. 중복이 섞여 들어온 targetId 리스트를 여기서 순수 고유 ID 리스트로 변환 (정규화)
        List<Long> uniqueTargetIds = command.targetId().stream().distinct().toList();

        // 1. 요청된 알림 마스터(NotificationJpaEntity)들 한 번에 조회
        List<NotificationJpaEntity> notifications = notificationRepository.findAllByIdIn(uniqueTargetIds);

        // 실패 4 — 404 미존재 검증 (요청한 개수와 DB에서 찾은 개수가 다르면 없는 게 섞여 있음)
        if (notifications.size() != uniqueTargetIds.size()) {
            throw new FMResourceNotFoundException("존재하지 않는 알림이 포함되어 있습니다.");
        }

        // 권한 상태의 기본값을 true로 두고 검증 결과에 따라 스위칭합니다.
        boolean hasGeneralAccess = true;
        boolean hasMsgNotiAccess = true;

        List<NotificationJpaEntity> generalNotisToDelete = new ArrayList<>();
        List<Long> messageRoomIds = new ArrayList<>();

        // 2. 알림 권한 체크 및 타입 분기
        // 2. 알림별 권한 검증 및 분기
        for (NotificationJpaEntity noti : notifications) {

            // A. 일반 알림 처리
            if (!"MESSAGE".equals(noti.getType())) {
                // 내 알림이 맞는지 유무를 불린으로 판단하여 정책 클래스에 위임
                // 내 알림이 아니라면 일반 알림 권한 플래그를 false로 변경
                if (!noti.getUserId().getId().equals(command.userId())) {
                    hasGeneralAccess = false;
                    break;
                }
                generalNotisToDelete.add(noti);
            }
            // B. 메시지 알림 처리 (방 ID만 수집)
            else {
                messageRoomIds.add(noti.getRefId());
            }
        }

        if (!messageRoomIds.isEmpty()) {
            // 1. 레포지토리 조회할 때 내 유저 ID도 함께 전달
            List<MessageReadJpaEntity> myRoomMessageReads = notificationRepository
                    .findMessageReadsByRoomIdsAndUserId(messageRoomIds, command.userId());

            // 2. 내가 권한을 가진 방의 개수 계산
            long myAuthorizedRoomCount = myRoomMessageReads.stream()
                    .map(MessageReadJpaEntity::getRoomId) // 방 ID 추출
                    .distinct()
                    .count();

            // 3. 요청한 고유 방 개수와 내가 실제 권한을 가진 방 개수가 '전부 일치(All)'해야만 권한 허용!
            hasMsgNotiAccess = (myAuthorizedRoomCount == messageRoomIds.stream().distinct().count());
        }

        notificationEligibilityPolicy.removeNotification(hasGeneralAccess, hasMsgNotiAccess); // false면 정책에서 403 예외 발생

        // 4. 상태 변경이 필요한 진짜 안 읽은 데이터들만 최종 반영 (자원 낭비 원천 차단)
        if (!generalNotisToDelete.isEmpty()) {
            notificationRepository.deleteAllInBatch(generalNotisToDelete);
        }
        if (!messageRoomIds.isEmpty()) {
            notificationRepository.bulkMarkMessageNotificationsAsDeleted(messageRoomIds, command.userId());
        }

        notificationRepository.fastSaveChanges();
        log.info("[RemoveNotificationCommandService] 삭제 처리 완료 (일반: {}건, 메시지: {}건) -> 실시간 웹소켓 갱신 진행",
                generalNotisToDelete.size(), messageRoomIds.size());

        eventPublisher.publishEvent(new NotificationCreatedPublishedEvent(command.userId(), "ALL"));
        log.info("[RemoveNotificationCommandService] 유저 {}번 화면 실시간 데이터 브로드캐스팅 완료", command.userId());
    }
}
