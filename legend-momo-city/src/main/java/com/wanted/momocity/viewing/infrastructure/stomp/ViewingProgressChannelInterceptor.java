package com.wanted.momocity.viewing.infrastructure.stomp;

import com.wanted.momocity.auth.infrastructure.jwt.JwtTokenProvider;
import com.wanted.momocity.auth.infrastructure.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/*
* comment.
*  [역할]
*  STOMP CONNECT 시점에 JWT 검증 후 userId 를 세션에 저장
*  -
*  [흐름]
*  프론트 -> STOMP CONNECT (헤더에 JWT 포함) -> preSend() 에서 CONNECT 명령 감지
*  -> Authorization 헤더에서 JWT 추출 -> JwtTokenProvider 로 검증
*  -> userId 를 세션에 저장 -> 이후 @MessageMApping 에서 세션에서 userId 꺼내서 사용
*  -
*  [ChannelInterceptor 사용 이유]
*  STOMP 메세지가 채널을 통해 전달되기 전에 가로채서 처리
*  HTTP 필터와 동일한 역할을 STOMP 에서 담당
* */

@Slf4j
@Component
@RequiredArgsConstructor
public class ViewingProgressChannelInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    // preSend : 메세지가 채널로 전송되기 전에 호출
    // -> CONNECT 명령일 때만 JWT 검증 수행 -> 나머지 명령 (SEND, SUBSCRIBE) 은 그냥 통과
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(
                message, StompHeaderAccessor.class
        );

        if (accessor == null) {
            return message;
        }

        /*
        * CONNECT 명령일 때만 JWT 검증
        * 최초 연결 시 한 번만 검증 -> 이후 메세지는 세션에서 userId 꺼내서 사용
        * */

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

            /*
            * Authorization 헤더에서 JWT 추출
            * 프론트가 STOMP CONNECT 시 헤더에 포함해서 전송 -> "Bearer {token}" 형태
            * */

            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer")) {
                log.warn("[Viewing] STOMP CONNECT 실패 - Authorization 헤더 없음");
                throw new IllegalArgumentException("Authorization 헤더가 없습니다.");
            }

            String token = authHeader.substring(7);

            // JWT 검증 -> 유효하지 않으면 예외 발생 -> 연결 거부
            jwtTokenProvider.validateToken(token);

            /*
            * userId 세션에 저장 -> getAuthentication() 으로 CustomUSerDetails 추출
            * -> userId 를 세션 attributes 에 저장 -> 이후 @MessageMApping 에서 꺼내서 사용
            * */

            Authentication authentication = jwtTokenProvider.getAuthentication(token);
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Long userId = userDetails.getUserId();

            accessor.getSessionAttributes().put("userId", userId);
            accessor.setUser(authentication);

            log.debug("[Viewing] STOMP CONNECT 성공 | userId={}, sessionId={}",
                    userId, accessor.getSessionId());

        }
        return message;

    }

}
