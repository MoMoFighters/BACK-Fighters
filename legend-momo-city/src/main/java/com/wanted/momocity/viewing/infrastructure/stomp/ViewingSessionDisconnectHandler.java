package com.wanted.momocity.viewing.infrastructure.stomp;

import com.wanted.momocity.viewing.application.command.SaveProgressCommand;
import com.wanted.momocity.viewing.application.usecase.ViewingCommandUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class ViewingSessionDisconnectHandler {

    private final ViewingSessionRegistry sessionRegistry;
    private final ViewingCommandUseCase viewingCommandUseCase;
    // @EventListener(SessionDisconenectEvent.class)
    // STOMP 연결 끊김 시 자동 호출 -> SessionDisconnectEvent 에서 SessionId 추출
    @EventListener
    public void handleDisconnect (SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();

        // getAndRemove
        // 세션 정보 꺼내고 삭제 -> null 이면 진척도 저장 없이 종료 (영상 재생 없이 연결만 끊긴 경우)
        ViewingSessionRegistry.ViewingSessionInfo info
                = sessionRegistry.getAndRemove(sessionId);

        if (info == null) {
            log.debug("[Viewing] 세션 정보 없음, lastPositionSec 저장 생략 | sessionId={}", sessionId);
            return;
        }

        log.info("[Viewing] STOMP 연결 끊김 감지 | sessionId={}, userId={}, chapterId={}, lastPlaybackSeconds={}",
        sessionId, info.userId(), info.chapterId(), info.lastPlaybackSeconds());

        try {

            // lastPositionSec 저장
            // 마지막으로 받은 playbackSeconds 를 lastPositionSec 으로 전달
            // ViewingCommandService 내부에서 isCompleted 여부에 따라 저장 분기 처리
            viewingCommandUseCase.handle(
                    new SaveProgressCommand(
                    info.userId(),
                    info.lectureId(),
                    info.chapterId(),
                    // playbackSeconds = 진척도 업데이트
                    info.lastPlaybackSeconds(),
                    // lastPositionSec = 이어보기 위치 저장용
                    info.lastPlaybackSeconds()
            ));

            log.info("[Viewing] lastPositionSec 저장 완료 | userId={}, chapterId={}, lastPositionSec={}",
                    info.userId(), info.chapterId(), info.lastPlaybackSeconds());

        } catch (Exception e) {

            // 연결 끊김 시 저장 실패는 치명적이지 않음
            // -> 예외 발생해도 서버 장애로 이어지지 않도록 catch
            // -> 로그만 남기고 계속 진행
            log.warn("[Viewing] lastPositionSec 저장 실패 | userId={}, chapterId={} | 예외={}",
                    info.userId(), info.chapterId(), e.getMessage());
        }
}
}
