package com.wanted.momocity.study.infrastructure.event;

import com.wanted.momocity.study.domain.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

/*
 * comment.
 *  study 도메인 이벤트 6종을 받아서 STOMP 방 토픽("/sub/study/room/{roomId}")으로 브로드캐스트하는 핸들러
 *  community 도메인 컨벤션 그대로 따름: @TransactionalEventListener(AFTER_COMMIT) + @Async("domainEventExecutor")
 *  -> DB 트랜잭션이 실제로 커밋된 이후에만 방송해야, 롤백된 상태 변화를 프론트가 실시간으로 잘못 받는 걸 방지함
 *  -
 *  StudyStompInterceptor가 검증한 것과 동일한 destination("/sub/study/room/{roomId}")으로만 발행
 *  구독 권한 검증과 발행 경로가 항상 일치함 (별도 경로 관리 불필요)
 * */

@Slf4j
@Component
@RequiredArgsConstructor
public class StudyBroadcastEventHandler {

    // Spring이 제공하는 STOMP 메시지 발행 템플릿 - convertAndSend()로 특정 destination에 메시지를 쏨
    private final SimpMessagingTemplate messagingTemplate;

    private static final String ROOM_TOPIC_PREFIX = "/sub/study/room/";

    // 초대 수락으로 새 멤버가 입장했을 때
    @Async("domainEventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMemberJoined(MemberJoinedEvent event) {
        broadcast(event.roomId(), "MEMBER_JOINED", Map.of(
                "userId", event.userId()
        ));
        log.info("[StudyBroadcast] MEMBER_JOINED 전송 | roomId={}, userId={}", event.roomId(), event.userId());
    }

    // 자진 퇴장 (일반 퇴장 / 방장 위임 후 퇴장 / 마지막 인원 퇴장 3가지 분기 모두 이 이벤트를 탐)
    @Async("domainEventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMemberLeft(MemberLeftEvent event) {
        broadcast(event.roomId(), "MEMBER_LEFT", Map.of(
                "userId", event.userId()
        ));
        log.info("[StudyBroadcast] MEMBER_LEFT 전송 | roomId={}, userId={}", event.roomId(), event.userId());
    }

    // 방장이 멤버를 강퇴했을 때
    @Async("domainEventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMemberKicked(MemberKickedEvent event) {
        broadcast(event.roomId(), "MEMBER_KICKED", Map.of(
                "targetUserId", event.targetUserId(),
                "hostUserId", event.hostUserId()
        ));
        log.info("[StudyBroadcast] MEMBER_KICKED 전송 | roomId={}, targetUserId={}", event.roomId(), event.targetUserId());
    }

    // 방장이 나가서 다음 사람에게 자동 위임됐을 때
    @Async("domainEventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleHostChanged(HostChangedEvent event) {
        // [수정] event.newHostId() -> event.newHostUserId() (실제 필드명에 맞춤)
        broadcast(event.roomId(), "HOST_CHANGED", Map.of(
                "newHostUserId", event.newHostUserId()
        ));
        log.info("[StudyBroadcast] HOST_CHANGED 전송 | roomId={}, newHostUserId={}", event.roomId(), event.newHostUserId());
    }

    // 마지막 인원이 나가서 방이 종료(소프트딜리트)됐을 때
    @Async("domainEventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRoomEnded(RoomEndedEvent event) {
        broadcast(event.roomId(), "ROOM_ENDED", Map.of());
        log.info("[StudyBroadcast] ROOM_ENDED 전송 | roomId={}", event.roomId());
    }

    // 타이머 상태가 STUDYING <-> RESTING <-> null(종료)로 전환될 때
    // (본인이 다른 탭/기기에서 접속 중이어도 이 방 토픽을 통해 자동 동기화됨)
    @Async("domainEventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTimerStatusChanged(TimerStatusChangedEvent event) {
        broadcast(event.roomId(), "TIMER_STATUS_CHANGED", Map.of(
                "userId", event.userId(),
                // timerStatus가 null일 수 있음(타이머 완전 종료) - Map.of는 null value를 허용 안 하므로 문자열로 방어 처리
                "timerStatus", event.timerStatus() == null ? "NONE" : event.timerStatus().name()
        ));
        log.info("[StudyBroadcast] TIMER_STATUS_CHANGED 전송 | roomId={}, userId={}, status={}",
                event.roomId(), event.userId(), event.timerStatus());
    }

    // 공통 발행 로직 - 모든 핸들러가 이 메서드로 수렴
    private void broadcast(Long roomId, String type, Map<String, Object> data) {
        StudyRoomBroadcastMessage message = new StudyRoomBroadcastMessage(type, roomId, data);
        messagingTemplate.convertAndSend(ROOM_TOPIC_PREFIX + roomId, message);
    }

}
