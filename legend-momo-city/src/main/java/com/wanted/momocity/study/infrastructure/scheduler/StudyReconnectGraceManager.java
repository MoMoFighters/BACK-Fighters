package com.wanted.momocity.study.infrastructure.scheduler;

import com.wanted.momocity.study.application.member.timer.usecase.TimerCommandUseCase;
import com.wanted.momocity.study.domain.repository.GroupRoomMemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/*
 * comment.
 *  WebSocket DISCONNECT 발생 시 즉시 타이머를 정리하지 않고, N초(기본 30초) 유예시간을 둠
 *  - 그 안에 같은 유저가 재접속(CONNECT)하면 예약된 정리 작업을 취소 (네트워크 순단/새로고침 방어)
 *  - 유예시간이 끝날 때까지 재접속이 없으면 그때 실제로 타이머를 정리
 *  -
 *  userId 기준으로 추적 (세션ID는 재연결마다 바뀌므로 추적 키로 부적합)
 *  단일 서버 운영 기준 인메모리 방식 -> 서버가 여러 대로 늘어나면 Redis TTL 방식으로 전환 필요
 * */

@Slf4j
@Component
public class StudyReconnectGraceManager {

    private final ScheduledExecutorService scheduler;
    private final TimerCommandUseCase timerCommandUseCase;
    private final GroupRoomMemberRepository groupRoomMemberRepository;
    private final long graceSeconds;
    // userId -> 예약된 정리 작업. 재접속 시 여기서 찾아서 취소함
    private final Map<Long, ScheduledFuture<?>> pendingTasks = new ConcurrentHashMap<>();

    public StudyReconnectGraceManager(
            @Qualifier("studyDisconnectScheduler") ScheduledExecutorService scheduler,
            TimerCommandUseCase timerCommandUseCase,
            GroupRoomMemberRepository groupRoomMemberRepository,
            @Value("${study.websocket.reconnect-grace-seconds:30}") long graceSeconds
    ) {
        this.scheduler = scheduler;
        this.timerCommandUseCase = timerCommandUseCase;
        this.groupRoomMemberRepository = groupRoomMemberRepository;
        this.graceSeconds = graceSeconds;
    }

    // DISCONNECT 시점에 호출 - N초 뒤 정리 작업을 예약
    public void onDisconnect(Long userId) {
        // 혹시 이전에 걸려있던 예약이 있으면(비정상적으로 중복 DISCONNECT 등) 먼저 취소하고 새로 걺
        cancelPending(userId);

        ScheduledFuture<?> future = scheduler.schedule(
                () -> handleGraceExpired(userId),
                graceSeconds,
                TimeUnit.SECONDS
        );
        pendingTasks.put(userId, future);
        log.info("[StudyReconnectGrace] 유예 타이머 시작 | userId={}, graceSeconds={}", userId, graceSeconds);
    }

    // CONNECT(재접속) 시점에 호출 - 걸려있던 예약이 있으면 취소
    public void onReconnect(Long userId) {
        boolean canceled = cancelPending(userId);
        if (canceled) {
            log.info("[StudyReconnectGrace] 유예시간 내 재접속으로 정리 작업 취소됨 | userId={}", userId);
        }
    }

    private boolean cancelPending(Long userId) {
        ScheduledFuture<?> future = pendingTasks.remove(userId);
        if (future != null) {
            future.cancel(false);
            return true;
        }
        return false;
    }

    // 유예시간 만료 시 실제 실행되는 정리 로직
    private void handleGraceExpired(Long userId) {
        pendingTasks.remove(userId);
        try {
            // 만료 시점 기준으로 다시 조회 - 그 사이 이미 pause/end 됐을 수도 있으므로 최신 상태 확인
            groupRoomMemberRepository.findAllByUserIdAndStudying(userId).stream()
                    .findFirst()
                    .ifPresent(member -> {
                        timerCommandUseCase.pause(userId, member.getGroupRoomId());
                        log.info("[StudyReconnectGrace] 유예시간 만료로 타이머 자동 일시정지 | userId={}, roomId={}",
                                userId, member.getGroupRoomId());
                    });
        } catch (Exception e) {
            // 이미 다른 경로로 pause/end 됐거나 방을 나간 경우 등 - 정리할 게 없는 정상적인 상황일 수 있으므로 warn으로만 남김
            log.warn("[StudyReconnectGrace] 유예시간 만료 처리 중 예외 (이미 정리됐을 가능성) | userId={}, message={}",
                    userId, e.getMessage());
        }
    }

}
