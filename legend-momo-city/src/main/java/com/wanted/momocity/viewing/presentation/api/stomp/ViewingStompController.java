package com.wanted.momocity.viewing.presentation.api.stomp;

import com.wanted.momocity.viewing.application.command.SaveProgressCommand;
import com.wanted.momocity.viewing.application.usecase.ViewingCommandUseCase;
import com.wanted.momocity.viewing.infrastructure.stomp.ViewingSessionRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

/*
* comment.
*  [역할]
*  STOMP 메세지를 받아서 진척도 저장 UseCase 호출
*  -
*  [흐름]
*  프론트 -> /pub/viewing/progress 로 메세지 전송
*  -> @MessageMapping("/viewing/progress") 에서 수신
*  -> ViewingSessionRegistry 에 세션 정보 업데이트
*  -> ViewingCommandUseCase.handle() 호출
*  -
*  [@RestController 사용 이유]
*  - STOMP 핸들러는 HTTP 응답을 반환하지 않음
*  - @Restcontroller 는 HTTP 응답 전용
*  - @Controller 로 선언해야 @MessageMapping 이 정상 동작
* */

@Slf4j
@Controller
@RequiredArgsConstructor
public class ViewingStompController {

    private final ViewingCommandUseCase viewingCommandUseCase;
    private final ViewingSessionRegistry sessionRegistry;

    // @MessageMapping("/viewing/progress")
    // WebSocketConfig 에서 설정한 /pub prefix 포함 -> 실제 destination: /pub/viewing/progress
    @MessageMapping("/viewing/progress")
    public void handleProgress(
            ViewingProgressMessage message,
            // SimpMessageHeaderAccessor
            // STOMP 메시지 헤더에서 sessionId, userId 추출 -> HandshakeInterceptor 에서 세션에 저장한 userId 를 꺼냄
            SimpMessageHeaderAccessor headerAccessor
    ) {
        // sessionId
        // STOMP 연결마다 고유한 세션 ID -> 연결 끊김 이벤트에서 세션 정보 꺼낼 때 사용
        String sessionId = headerAccessor.getSessionId();

        // userId
        // STOMP CONNECT 시점에 JWT 검증 후 세션에 저장한 값 -> HeadAccessor 로 꺼냄
        Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");

        if (userId == null) {
            log.warn("[Viewing] STOMP 메시지 수신 실패 - userId 없음 | sessionId={}", sessionId);
            return;
        }

        log.debug("[Viewing] STOMP 진척도 메시지 수신 | sessionId={}, userId={}, chapterId={}, playbackSeconds={}",
                sessionId, userId, message.chapterId(), message.playbackSeconds());

        // 세션 정보 업데이트 -> 연결 끊김 시 lastPositionSec 저장에 사용
        sessionRegistry.saveOrUpdate(
                sessionId,
                userId,
                message.lectureId(),
                message.chapterId(),
                message.playbackSeconds()
        );

        // 진척도 저장 UseCase 호출 -> 기존 HTTP 방식과 동일한 로직 재사용
        viewingCommandUseCase.handle(new SaveProgressCommand(
                userId,
                message.lectureId(),
                message.chapterId(),
                message.playbackSeconds(),
                // lastPositionSec 연결 끊김 시 별도 저장
                null
        ));

    }

}
