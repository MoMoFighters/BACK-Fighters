package com.wanted.momocity.notification.application.service;

import com.wanted.momocity.message.application.usecase.MessageQueryUseCase;
import com.wanted.momocity.message.infrastructure.persistence.MessageJpaEntity;
import com.wanted.momocity.message.infrastructure.persistence.MessageReadJpaEntity;
import com.wanted.momocity.notification.application.manager.NotificationSessionManager;
import com.wanted.momocity.notification.application.query.GetMainTotalCountsQuery;
import com.wanted.momocity.notification.application.query.GetNotificationQuery;
import com.wanted.momocity.notification.application.query.GetPhoneAppCountsQuery;
import com.wanted.momocity.notification.application.usecase.NotificationQueryUseCase;
import com.wanted.momocity.notification.domain.repository.NotificationRepository;
import com.wanted.momocity.notification.infrastructure.persistence.NotificationJpaEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
    //웹소켓 브로드캐스팅 템플릿 주입
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationSessionManager notificationSessionManager;

    //알림 목록
    @Override
    public List<NotiView> getNotificationQueryHandle(GetNotificationQuery query) {
        // 🎯 [여기 딱 한 줄만 차단!]: 알림 목록 가공은 무거우니까 유저가 화면에 없을 때만 연산 자체를 컷합니다.
        if (!notificationSessionManager.isUserSubscribed(query.userId())) {
            log.info("[알림 쿼리] 유저 {}번이 구독 중이 아니므로 무거운 가공 연산을 스킵합니다.", query.userId());
            return Collections.emptyList();
        }

        log.info("[GetNotificationQueryService] 알림 목록 조회 시작 - 유저ID:{}", query.userId());

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
            String finalCombinedMessage = latestNoti.getMessage();
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
                    }
                }
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

        // 3. 최종 전체 목록 최신순 정렬
        finalResultList.sort((a, b) -> b.createdAt().compareTo(a.createdAt()));

        // 🎯 [핵심]: 조회된 알림 목록을 즉시 웹소켓 채널로 발송! (순서, 내용, 시간 다 가공된 상태)
        messagingTemplate.convertAndSendToUser(query.userId().toString(), "/sub/notice/list", finalResultList);
        log.info("[알림 쿼리 웹소켓] 유저 {}번에게 가공된 알림 목록 실시간 전송 완료", query.userId());

        return finalResultList;
    }

    //메인페이지 종 총 알림 개수
    @Override
    public MainTotalCountsView getMainTotalCountsQueryHandle(GetMainTotalCountsQuery query) {
        log.info("[GetMainTotalCountsQueryService] 전체 알림 개수 조회 시작 - 유저ID:{}", query.userId());

        // 1. 일반 알림(MESSAGE 제외) 중 안 읽은 개수 (α)
        long generalCount = notificationRepository.countUnreadGeneral(query.userId());

        // 2. 삭제 안되고, 안 읽은 메시지가 쌓여있는 채팅방의 개수 (β) = 알림 목록에 뜬 메시지 관련 알림 수
        long messageRoomCount = notificationRepository.countUnreadMessageRooms(query.userId());

        // 3. 최종 합산 (α + β)
        long totalCount = generalCount + messageRoomCount;

        MainTotalCountsView response = new MainTotalCountsView(totalCount);

        // 🎯 [핵심]: 조회된 안읽은 총 개수를 즉시 웹소켓 채널로 발송!
        messagingTemplate.convertAndSendToUser(query.userId().toString(), "/sub/notice/total-counts", response);
        log.info("[알림 쿼리 웹소켓] 유저 {}번에게 안읽은 총 알림 개수({}) 실시간 전송 완료", query.userId(), totalCount);

        log.info("[GetMainTotalCountsQueryService] 조회 완료 - 일반 알림 줄수: {}, 메시지 안읽은 방수: {}, 총 배지수: {}",
                generalCount, messageRoomCount, totalCount);
        return new MainTotalCountsView(totalCount);
    }

    //휴대폰 속 앱별 알림 개수(친구+메시지, 캘린더, 커뮤니티)
    @Override
    public PhoneAppCountsView getPhoneAppCountsQueryHandle(GetPhoneAppCountsQuery query) {
        log.info("[GetPhoneAppCountsQueryService] 휴대폰 속 앱별 알림 개수 실제 DB 조회 시작 - 유저ID:{}", query.userId());

        Long userId = query.userId();

        // 1. 캘린더 알림 개수 (type = 'CALENDAR' 이면서 내 것)
        long calendarCount = notificationRepository.countByUserIdAndType(userId, "CALENDAR");

        // 2. 커뮤니티 알림 개수 (type = 'POST' 이면서 내 것)
        long communityCount = notificationRepository.countByUserIdAndType(userId, "POST");

        // 3. 친구 요청 알림 개수 (type = 'FRIEND_REQUEST' 이면서 내 것)
        long friendRequestCount = notificationRepository.countByUserIdAndType(userId, "FRIEND_REQUEST");

        // 4. 🔥 안 읽은 메시지 전체 개수 (message_read 테이블에서 isMsgRead = false 인 건수)
        // 💡 룸 개수가 아니라 '쌓인 메시지 총 개수'를 가져오는 포트/어댑터 메서드로 매핑합니다.
        long totalUnreadMessageCount = notificationRepository.countTotalUnreadMessages(userId);

        // 5. 최종 메시지 + 친구 요청 합산
        long totalMsgFriendCount = totalUnreadMessageCount + friendRequestCount;

        log.info("[GetPhoneAppCountsQueryService] 앱별 알림 조회 완료 -> 메시지(총 {}건)+친구({}건): {}, 캘린더: {}, 커뮤니티: {}",
                totalUnreadMessageCount, friendRequestCount, totalMsgFriendCount, calendarCount, communityCount);

        // 🎯 [반환 객체 생성 및 변수 통일]
        PhoneAppCountsView response = new PhoneAppCountsView(totalMsgFriendCount, calendarCount, communityCount);

        // 🎯 [핵심 웹소켓 실시간 발송]: 정형화된 채널 주소(/sub/notice/app-counts)로 가공 데이터를 브로드캐스팅합니다.
        messagingTemplate.convertAndSendToUser(userId.toString(), "/sub/notice/app-counts", response);
        log.info("[알림 쿼리 웹소켓] 유저 {}번에게 각 휴대폰 앱별 안읽은 알림 데이터 실시간 전송 완료", userId);

        return response;
    }
}
