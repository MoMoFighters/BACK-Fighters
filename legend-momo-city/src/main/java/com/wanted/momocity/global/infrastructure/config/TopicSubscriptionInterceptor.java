package com.wanted.momocity.global.infrastructure.config;

import com.wanted.momocity.auth.application.port.LoadUserPort;
import com.wanted.momocity.auth.domain.model.User;

import com.wanted.momocity.message.application.manager.ChatRoomSessionManager;
import com.wanted.momocity.notification.application.manager.NotificationSessionManager;
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

    //웹소켓
    /*채팅방 관련
    * -(메시지 내역 - 실시간 전송 시 내역, 말풍선별 안읽은 사람 수, 나가기 안내 문구)
    * -(채팅방 목록 - 메시지, 채팅방별 안읽음 개수, 날짜, 정렬, 방이름, 닉네임)
    */
    private final ChatRoomSessionManager sessionManager;
    //알림 관련 - 메인 페이지 종 모양에 띄워질 총 알림 개수
    private final NotificationSessionManager notificationSessionManager;
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

            // 알림 개수 채널 구독 처리
            // 프론트가 /user/sub/notice/total-counts로 구독 시, 내장 브러커 통과 경로 매칭
            if (destination != null && destination.contains("/sub/notice/total-counts")) {
                notificationSessionManager.enterNotificationChannel(userId, accessor.getSessionId());
                log.info("[웹소켓 인터셉터] 유저 {}번이 실시간 알림 개수 채널을 구독했습니다.", userId);
            }
        }

        //프론트엔드가 웹소켓 연결을 끊거나 방을 나갈 때
        //채팅방 주소 구독을 취소하거나(UNSUBSCRIBE = 뒤로가기), 웹소켓 연결 자체가 끊어질 때(DISCONNECT = 앱 종료)
        // 1. UNSUBSCRIBE 처리 (뒤로가기 등으로 특정 채널 구독 해제할 때)
        if (StompCommand.UNSUBSCRIBE.equals(command)) {
            Long userId = getUserIdFromAccessor(accessor);

            // 🎯 [핵심] STOMP 명세상 accessor.getDestination()이 null이어도,
            // 네이티브 메시지 헤더 내부에는 원래 구독 주소 정보가 남아있습니다.
            String destination = null;
            Object simpDestination = accessor.getMessageHeaders().get("simpDestination");
            if (simpDestination != null) {
                destination = simpDestination.toString();
            }

            if (userId != null && destination != null) {
                // 실제 채팅방 상세 채널(/sub/chat/room/)을 나갈 때만 세션에서 제거!
                if (destination.startsWith("/sub/chat/room/")) {
                    sessionManager.leaveRoom(userId);
                    log.info("[웹소켓 인터셉터] 유저 {}번이 채팅방({}) 구독을 취소하여 세션에서 제거되었습니다.", userId, destination);
                } // 2) 알림 채널 구독 해제
                else if (destination.contains("/sub/notice/total-counts")) {
                    notificationSessionManager.leaveNotificationChannel(userId, accessor.getSessionId());
                    log.info("[웹소켓 인터셉터] 유저 {}번이 실시간 알림 개수 채널 구독을 취소했습니다.", userId);
                }
                else {
                    log.info("[웹소켓 인터셉터] 일반 채널 구독 취소이므로 방 세션을 유지합니다. (경로: {})", destination);
                }
            }
        }

        // 2. DISCONNECT 처리 (앱 종료, 웹소켓 연결 자체가 끊길 때)
        if (StompCommand.DISCONNECT.equals(command)) {
            Long userId = getUserIdFromAccessor(accessor);
            if (userId != null) {
                // 연결이 완전히 끊기는 것은 방을 나가는 것이 맞으므로 무조건 제거
                sessionManager.leaveRoom(userId);

                notificationSessionManager.leaveNotificationChannel(userId, accessor.getSessionId());
                log.info("[웹소켓 인터셉터] 유저 {}번의 웹소켓 연결이 종료되어 세션에서 완전히 제거되었습니다.", userId);
            }
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
