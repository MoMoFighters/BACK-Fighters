package com.wanted.momocity.notification.application.service;

import com.wanted.momocity.message.application.usecase.MessageQueryUseCase;
import com.wanted.momocity.message.infrastructure.persistence.MessageJpaEntity;
import com.wanted.momocity.message.infrastructure.persistence.MessageReadJpaEntity;
import com.wanted.momocity.notification.application.query.GetNotificationQuery;
import com.wanted.momocity.notification.application.usecase.NotificationQueryUseCase;
import com.wanted.momocity.notification.domain.repository.NotificationRepository;
import com.wanted.momocity.notification.infrastructure.persistence.NotificationJpaEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class NotificationQueryService implements NotificationQueryUseCase {

    private final NotificationRepository notificationRepository;

    //알림 목록
    @Override
    public List<NotiView> getNotificationQueryHandle(GetNotificationQuery query) {
        log.info("[NotificationQueryService] 알림 목록 조회 시작 - 유저ID:{}", query.userId());

        // 1. 개발자님이 완성하신 명칭과 조인 쿼리로 DB에서 데이터를 싹 들고옵니다.
        List<Object[]> rawLogs = notificationRepository.findAllByUserId(query.userId());

        List<NotiView> finalResultList = new ArrayList<>();

        // 메시지 가공을 위해 방별로 데이터를 묶을 맵 (Key: roomId, Value: 그 방에 속한 Object[] 로그 행들)
        Map<Long, List<Object[]>> messageGroupByRoom = new HashMap<>();

        for (Object[] row : rawLogs) {
            NotificationJpaEntity noti = (NotificationJpaEntity) row[0];
            MessageReadJpaEntity mr = (MessageReadJpaEntity) row[1]; // LEFT JOIN 결과

            if ("MESSAGE".equals(noti.getType())) {
                // 메시지 알림인데 내 전용 message_read 행이 없다면 나한테 온 게 아니거나 지운 방이므로 컷
                if (mr == null) {
                    continue;
                }
                // 방 번호(refId) 기준으로 로우 데이터(Object[])들을 차곡차곡 수집
                messageGroupByRoom.computeIfAbsent(noti.getRefId(), k -> new ArrayList<>()).add(row);

            } else {
                // 일반 알림들은 걸러낼 필요 없이 즉시 최종 바구니에 추가
                finalResultList.add(new NotiView(
                        noti.getId(),
                        noti.getType(),
                        noti.getMessage(),
                        noti.getIsRead(), // 일반 알림은 원래 노티 마스터의 읽음 플래그 적용
                        noti.getRefId(),
                        noti.getCreatedAt()
                ));
            }
        }

        // 2. 🔥 메시지 알림 합치기 및 읽음 상태 동기화
        for (Map.Entry<Long, List<Object[]>> entry : messageGroupByRoom.entrySet()) {
            Long roomId = entry.getKey();
            List<Object[]> roomRows = entry.getValue();

            // 🚨 [검증]: 가져온 알림 목록 중 "isNotiRead가 false(안읽음)"인 건이 하나라도 존재하나요?
            boolean hasUnread = roomRows.stream()
                    .map(row -> (MessageReadJpaEntity) row[1])
                    .anyMatch(mr -> !mr.isNotiRead()); // 👈 하나라도 안 읽었으면 true가 됨

            // 시간순 정렬 후 가장 최근 알림 마스터 정보 채택
            roomRows.sort((a, b) -> ((NotificationJpaEntity) b[0]).getCreatedAt()
                    .compareTo(((NotificationJpaEntity) a[0]).getCreatedAt()));
            NotificationJpaEntity latestNoti = (NotificationJpaEntity) roomRows.get(0)[0];

            // 🚨 [핵심]: notification의 userId(발신자) 닉네임 수집
            List<String> senders = roomRows.stream()
                    .map(row -> ((NotificationJpaEntity) row[0]).getUserId().getNickname())
                    .distinct()
                    .toList();

            // DB에서 룸 타이틀 조회
            String roomTitle = notificationRepository.findRoomTitleById(roomId).orElse(null);
            // 🚨 [문구 가공]: notification 테이블의 userId(발신자)를 꺼내서 닉네임들을 조립
            String finalCombinedMessage;
            if (roomRows.size() > 1) {

                //다대다이면서 발신인 1명 이상
                if (!(roomTitle == null || roomTitle.isEmpty())) {
                    if (senders.size() >= 3) {
                        // 3명 이상일 때: [방이름] 누구, 누구 외 새로운 메시지가 있습니다.
                        String topSenders = String.join(", ", senders.subList(0, 2));
                        finalCombinedMessage = String.format("[%s] %s 외 새로운 메시지가 있습니다.", roomTitle, topSenders);
                    } else if (senders.size() == 2) {
                        // 딱 2명일 때: [방이름] 누구, 누구님이 메시지를 보냈습니다.
                        String topSenders = String.join(", ", senders);
                        finalCombinedMessage = String.format("[%s] %s님이 메시지를 보냈습니다.", roomTitle, topSenders);
                    } else {
                        // 🌟 [버그 해결]: 다대다 방인데 쌓인 알림이 1개거나 발신자가 1명인 경우
                        // [방이름] 누구님이 메시지를 보냈습니다. (혹은 최신 메시지 내용 노출)
                        finalCombinedMessage = String.format("[%s] %s", roomTitle, latestNoti.getMessage());
                    }
                } else {
                    // 2) 일대일 방이거나 한 명이 연속으로 보낸 경우 -> 원래 세팅된 메시지 포맷 그대로 유지
                    finalCombinedMessage = latestNoti.getMessage();
                }


                // 한 줄로 압축 완성된 메시지 알림을 최종 결과창에 골인
                finalResultList.add(new NotiView(
                        latestNoti.getId(),
                        "MESSAGE",
                        finalCombinedMessage,
                        !hasUnread, // 하나라도 안 읽은 게 있으면 최종 상태는 false(안읽음 표시)
                        roomId,
                        latestNoti.getCreatedAt()
                ));
            }
        }

        // 3. 최종 전체 목록 최신순 정렬
        finalResultList.sort((a, b) -> b.createdAt().compareTo(a.createdAt()));

        return finalResultList;
    }
}
