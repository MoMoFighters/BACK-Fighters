package com.wanted.momocity.global.infrastructure.config;

import com.wanted.momocity.auth.application.port.LoadUserPort;
import com.wanted.momocity.auth.domain.model.User;

import com.wanted.momocity.message.application.manager.ChatRoomSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
@RequiredArgsConstructor
@Slf4j
public class TopicSubscriptionInterceptor implements ChannelInterceptor {

    private final ChatRoomSessionManager sessionManager;
    private final LoadUserPort loadUserPort;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        StompCommand command = accessor.getCommand();

        //프론트엔드가 웹소켓 연결 후 특정 방을 구독할 때 주소 가로채기
        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            String destination = accessor.getDestination();
            Long userId = getUserIdFromAccessor(accessor); //세선이나 헤더에서 로그인 유저ID 추출

            if (destination != null && destination.startsWith("/sub/chat/room/")) {
                String roomIdStr = destination.replace("/sub/chat/room/", "");
                Long roomId = Long.parseLong(roomIdStr);

                //세션 매니저에 "이 유저 들어왔다"고 기록
                sessionManager.enterRoom(userId, roomId);
                log.info("[웹소켓 인터셉터] 유저 {}번이 {}번 채팅방에 입장했습니다.", userId, roomId);
            }
        }

        //프론트엔드가 웹소켓 연결을 끊거나 방을 나갈 때
        if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
            Long userId = getUserIdFromAccessor(accessor);
            sessionManager.leaveRoom(userId);
            log.info("[웹소켓 인터셉터] 유저 {}이 채팅방에서 퇴장했습니다.", userId);
        }
        return message;
    }

    //JWT 토큰 기반의 시큐리티 컨텍스트에서 유저ID 추출하기
    private Long getUserIdFromAccessor(StompHeaderAccessor accessor) {
        //세션/헤더 값에서 빼내어 사용
        Principal principal = accessor.getUser();

        if (principal == null) {
            log.warn("[웹소켓 인터셉터] 인증 정보가 존재하지 않는 접근입니다.");
            return null;
        }

        // 1. 스프링 시큐리티 기본 User 객체 안에서 username(이메일)을 추출합니다.
        // principal.getName()을 호출하면 담당자님이 세팅한 user.getEmail() 값이 튀어나옵니다.
        String email = principal.getName();

        if (email == null || email.isBlank()) {
            log.warn("[웹소켓 인터셉터] 식별 가능한 이메일 정보가 없습니다.");
            return null;
        }

       try {
           // 2. 가로챈 이메일을 가지고 LoadUserPort를 찔러서 우리 도메인의 User 객체를 꺼냅니다.
           User user = loadUserPort.findByEmail(email)
                   .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저 이메일입니다: " + email));

           // 3. 드디어 찾은 진짜 유저의 식별 PK ID(Long)를 리턴! 🎯
           return user.getId();

       } catch (Exception e) {
           log.error("[웹소켓 인터셉터] 이메일로 유저 ID를 조회하는 중 실패했습니다. 이메일: {}, 에러: {}", email, e.getMessage());
           throw new IllegalArgumentException("유저 정보 조회 실패");
       }
    }
}
