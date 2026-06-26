package com.wanted.momocity.notification.application.service;

import com.wanted.momocity.friend.fmexception.FMResourceNotFoundException;
import com.wanted.momocity.message.infrastructure.persistence.MessageReadJpaEntity;
import com.wanted.momocity.notification.application.command.ReadNotificationCommand;
import com.wanted.momocity.notification.application.policy.NotificationEligibilityPolicy;
import com.wanted.momocity.notification.application.query.GetMainTotalCountsQuery;
import com.wanted.momocity.notification.application.query.GetNotificationQuery;
import com.wanted.momocity.notification.application.query.GetPhoneAppCountsQuery;
import com.wanted.momocity.notification.application.usecase.NotificationCommandUseCase;
import com.wanted.momocity.notification.application.usecase.NotificationQueryUseCase;
import com.wanted.momocity.notification.domain.repository.NotificationRepository;
import com.wanted.momocity.notification.infrastructure.persistence.NotificationJpaEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    //알림 읽기
    @Override
    public void readNotificationCommandHandle(ReadNotificationCommand command) {

        log.info("[NotificationCommandService] 알림 읽음 처리 시작 - 유저ID: {}, 대상 알림개수: {}", command.userId(), command.targetId().size());

        // 🎯 [정책 위임 변경]: 400 빈 값 검증 정책을 최상단에서 호출하여 공회전 방지!
        notificationEligibilityPolicy.validateReadRequest(command.targetId());

        // 1. 요청된 알림 마스터(NotificationJpaEntity)들 한 번에 조회
        List<NotificationJpaEntity> notifications = notificationRepository.findAllByIdIn(command.targetId());

        // ❌ 실패 4 — 404 미존재 검증 (요청한 개수와 DB에서 찾은 개수가 다르면 없는 게 섞여 있음)
        if (notifications.size() != command.targetId().size()) {
            throw new FMResourceNotFoundException("존재하지 않는 알림이 포함되어 있습니다.");
        }

        // 🎯 권한 상태의 기본값을 true로 두고 검증 결과에 따라 스위칭합니다.
        boolean hasGeneralAccess = true;
        boolean hasMsgNotiAccess = true;

        List<NotificationJpaEntity> generalNotisToUpdate = new ArrayList<>();
        List<Long> messageRoomIds = new ArrayList<>();

        // 2. 알림 권한 체크 및 타입 분기
        // 2. 알림별 권한 검증 및 분기
        for (NotificationJpaEntity noti : notifications) {

            // 📌 A. 일반 알림 처리
            if (!"MESSAGE".equals(noti.getType())) {
                // 내 알림이 맞는지 유무를 불린으로 판단하여 정책 클래스에 위임
                // 내 알림이 아니라면 일반 알림 권한 플래그를 false로 변경
                if (!noti.getUserId().getId().equals(command.userId())) {
                    hasGeneralAccess = false;
                }

                // 아직 안 읽은 일반 알림만 수정 대상으로 수집 (자원 절약)
                if (!noti.getIsRead()) {
                    noti.markAsRead();
                    generalNotisToUpdate.add(noti);
                }
            }
            // 📌 B. 메시지 알림 처리 (방 ID만 수집)
            else {
                messageRoomIds.add(noti.getRefId());
            }
        }

        // 3. 메시지 알림 권한 검증 및 수정 타겟 추출
//        List<MessageReadJpaEntity> messageReadsToUpdate = new ArrayList<>();

        if (!messageRoomIds.isEmpty()) {
            // 🎯 notification의 refId(방 ID 목록)를 기준으로 관련 message_read 테이블 행들을 전부 조회
            List<MessageReadJpaEntity> roomMessageReads = notificationRepository
                    .findMessageReadsByRoomIds(messageRoomIds); // ✨ 유저 아이디 조건 없이 방 기준으로만 조회

            // 🎯 [메시지 알림 권한 검증]:
            // 조회된 전체 행 중에서 message_read의 userId가 로그인 유저인 건이 '하나라도' 존재하는지 불린 검증
            // 🎯 [메시지 알림 권한 판별]: 내 아이디가 룸 멤버 목록에 존재하는지 체크 후 플래그 반영
            hasMsgNotiAccess = roomMessageReads.stream()
                    .anyMatch(mr -> mr.getUserId().getId().equals(command.userId()));

        }

        notificationEligibilityPolicy.readNotification(hasGeneralAccess, hasMsgNotiAccess); // false면 정책에서 403 예외 발생

        // 4. 상태 변경이 필요한 진짜 안 읽은 데이터들만 최종 반영 (자원 낭비 원천 차단)
        if (!generalNotisToUpdate.isEmpty()) {
            notificationRepository.saveAll(generalNotisToUpdate);
        }
        if (!messageRoomIds.isEmpty()) {
            notificationRepository.bulkMarkMessageNotificationsAsRead(messageRoomIds, command.userId());
        }

        notificationRepository.fastSaveChanges();
        log.info("[NotificationCommandService] 읽음 처리 완료 (일반: {}건, 메시지: {}건) -> 실시간 웹소켓 갱신 진행",
                generalNotisToUpdate.size(), messageRoomIds.size());

        // 5. 🎯 [유기적 웹소켓 연동]: 지적하신 대로 불필요한 try-catch를 완전히 제거하고 깔끔하게 3종 호출
        // 알림 목록(list) 실시간 전송 트리거
        notificationQueryUseCase.getNotificationQueryHandle(new GetNotificationQuery(command.userId()));

        // 상단 종 모양 배지 수(total-counts) 실시간 전송 트리거
        notificationQueryUseCase.getMainTotalCountsQueryHandle(new GetMainTotalCountsQuery(command.userId()));

        // 앱별 배지 수(app-counts) 실시간 전송 트리거
        notificationQueryUseCase.getPhoneAppCountsQueryHandle(new GetPhoneAppCountsQuery(command.userId()));

        log.info("[NotificationCommandService] 유저 {}번 화면 실시간 데이터 브로드캐스팅 완료", command.userId());
    }
}
