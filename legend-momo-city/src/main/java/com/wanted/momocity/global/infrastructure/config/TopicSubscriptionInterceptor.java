package com.wanted.momocity.global.infrastructure.config;

import com.wanted.momocity.auth.application.port.BlacklistPort;
import com.wanted.momocity.auth.application.port.LoadUserPort;
import com.wanted.momocity.auth.domain.model.User;

import com.wanted.momocity.auth.infrastructure.jwt.JwtTokenProvider;
import com.wanted.momocity.message.application.manager.ChatRoomSessionManager;
import com.wanted.momocity.message.application.manager.ChatTypingBroadcaster;
import com.wanted.momocity.message.application.manager.ChatTypingSessionManager;
import com.wanted.momocity.notification.application.manager.NotificationSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
    private final JwtTokenProvider jwtTokenProvider;
    private final BlacklistPort blacklistPort;

    // 🎯 [핵심 추가]: 세션ID + 구독ID 조합을 key로 삼아 실제 destination 주소를 기억하는 메모리 지도
    private final Map<String, String> subscriptionRegistry = new ConcurrentHashMap<>();

    private final ChatTypingSessionManager typingSessionManager;

    private final ObjectProvider<ChatTypingBroadcaster> typingBroadcasterProvider;

//    private final ChatTypingBroadcaster typingBroadcaster;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        StompCommand command = accessor.getCommand();
        String sessionId = accessor.getSessionId();

        // 1. 프론트엔드가 최초 연결(CONNECT)할 때 토큰 인증 및 Principal 세팅
        if (StompCommand.CONNECT.equals(command)) {
            // 1. 기존 방식대로 헤더에서 먼저 토큰을 찾아봅니다. (로컬 테스트용)
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            String token = null;

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7).trim();
            } else {
                // 🎯 2. [배포 환경 우회용] 헤더에 없다면 쿼리 스트링(?token=xxxx)에서 추출합니다.
                // STOMP CONNECT 프레임의 nativeHeaders나 simpConnectMessage의 쿼리 파라미터를 활용
                Object simpConnectMessage = accessor.getHeader("simpConnectMessage");
                if (simpConnectMessage instanceof Message<?>) {
                    // 웹소켓 연결 주소 뒤에 붙은 쿼리 파라미터를 파싱하는 로직
                    String nativeHeaderUrl = accessor.getFirstNativeHeader("Authorization");
                    // 위 방법 대신, 프론트엔드에서 'connectHeaders' 내부에 파라미터를 실어 보낼 수도 있습니다.
                    token = accessor.getFirstNativeHeader("token"); // 프론트가 connectHeaders: { token: '...' } 로 보낼 경우
                }
            }

            // 만약 주소 창 쿼리 스트링으로 들어온다면 accessor에서 직접 꺼낼 수도 있습니다.
            if (token == null) {
                // 가장 확실한 방법: 프론트가 최초 stompClient.connect({ token: '토큰값' }, ...)
                // 형태로 헤더 대신 커스텀 Key로 직접 찔러 넣어주게 조율하는 것이 좋습니다.
                token = accessor.getFirstNativeHeader("token");
            }

            if (token == null) {
                log.warn("[웹소켓] CONNECT 인증 토큰을 찾을 수 없습니다.");
                return null; // 연결 거부
            }

            try {
                if (blacklistPort.isBlacklisted(token) || !jwtTokenProvider.validateToken(token)) {
                    log.warn("[웹소켓] CONNECT 토큰 검증 실패");
                    return null;
                }
                        // 토큰이 유효하면 Authentication 객체를 가져옴
                        org.springframework.security.core.Authentication authentication =
                                jwtTokenProvider.getAuthentication(token);

                        // 🎯 핵심: STOMP 세션에 유저 Principal을 강제로 주입!
                        // 이렇게 해야 이후 SUBSCRIBE나 다른 프레임에서 accessor.getUser()로 꺼낼 수 있음
                        accessor.setUser(authentication);

                        // 세션 어트리뷰트에도 보관
                        if (accessor.getSessionAttributes() != null) {
                            com.wanted.momocity.auth.infrastructure.security.CustomUserDetails userDetails =
                                    (com.wanted.momocity.auth.infrastructure.security.CustomUserDetails) authentication.getPrincipal();
                            accessor.getSessionAttributes().put("userId", userDetails.getUserId());
                        }
                        log.info("[웹소켓] CONNECT 시점 인증 성공. 유저 인증 객체 등록 완료.");
            } catch (Exception e) {
                log.error("[웹소켓] CONNECT 토큰 인증 실패: {}", e.getMessage());
                return null; // 연결 거부
            }
        }

        //프론트엔드가 웹소켓 연결 후 특정 방을 구독할 때 주소 가로채기
        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            String destination = accessor.getDestination();
            String subId = accessor.getSubscriptionId();
            Long userId = getUserIdFromAccessor(accessor); //세선이나 헤더에서 로그인 유저ID 추출

            if (userId == null) {
                log.warn("[웹소켓 인터셉터] 인증 실패로 구독 거부됨: {}", destination);
                return null; // 🚨 프론트 브로커 에러 유발 차단 및 거부
            }

            // 🎯 [추가]: 나중에 UNSUBSCRIBE 할 때 찾아 쓰려고 장부에 적어둠
            if (sessionId != null && subId != null && destination != null) {
                subscriptionRegistry.put(sessionId + "_" + subId, destination);
            }

            // 1. 메시지 내역 조회 (/user/sub/chat/room/{roomId})
            if (destination != null && destination.contains("/chat/room/")) {
                String roomIdStr = destination.substring(destination.lastIndexOf("/") + 1);
                try {
                    Long roomId = Long.parseLong(roomIdStr);
                    sessionManager.enterRoom(userId, roomId);
                    log.info("[웹소켓 인터셉터] 유저 {}번이 {}번 채팅방에 입장했습니다.", userId, roomId);
                } catch (NumberFormatException e) {
                    log.error("[웹소켓 인터셉터] 채팅방 ID 파싱 실패 주소: {}", destination);
                }
            }

            // 2. 🎯 [핵심 수정] 종 모양 알림 개수 채널 구독 처리
            // 스프링이 주소를 변환하므로 앞의 접두사를 제외하고 핵심 키워드로만 낚아챕니다.
            if (destination != null && destination.contains("/notice/total-counts")) {
                notificationSessionManager.enterNotificationChannel(userId, accessor.getSessionId());
                log.info("[웹소켓 인터셉터] 유저 {}번이 실시간 알림 개수 채널을 구독했습니다. (최종매핑주소: {})", userId, destination);
            }

            // 3. 🎯 채팅방 목록, 휴대폰 속 앱별 알림 개수, 일반 알림 목록 공통 안전 통과 처리
            if (destination != null && (
                    destination.contains("/chat/rooms") ||
                            destination.contains("/notice/app-counts") ||
                            destination.contains("/notice/list") ||
                            destination.contains("/notice/notificationlist")
            )) {
                log.info("[웹소켓 인터셉터] 유저 {}번이 공통 수신 채널을 안전하게 구독했습니다: {}", userId, destination);
            }
        }

        //프론트엔드가 웹소켓 연결을 끊거나 방을 나갈 때
        //채팅방 주소 구독을 취소하거나(UNSUBSCRIBE = 뒤로가기), 웹소켓 연결 자체가 끊어질 때(DISCONNECT = 앱 종료)
        // 1. UNSUBSCRIBE 처리 (뒤로가기 등으로 특정 채널 구독 해제할 때)
        if (StompCommand.UNSUBSCRIBE.equals(command)) {
            Long userId = getUserIdFromAccessor(accessor);
            String subId = accessor.getSubscriptionId();

            // 🎯 [핵심] STOMP 명세상 accessor.getDestination()이 null이어도,
            // 네이티브 메시지 헤더 내부에는 원래 구독 주소 정보가 남아있습니다.
            String destination = null;
            if (sessionId != null && subId != null) {
                destination = subscriptionRegistry.remove(sessionId + "_" + subId);
            }

            if (userId != null && destination != null) {
                // 실제 채팅방 상세 채널(/sub/chat/room/)을 나갈 때만 세션에서 제거!
                if (destination.contains("/chat/room/")) {
                    try {
                        // 주소 역추적 장부에서 해제하려는 방의 roomId를 정확하게 파싱해냅니다.
                        String roomIdStr = destination.substring(destination.lastIndexOf("/") + 1);
                        Long roomId = Long.parseLong(roomIdStr);
                        sessionManager.leaveRoom(userId, roomId);
                        typingSessionManager.stopTyping(roomId, userId);
                        typingBroadcasterProvider.getObject().broadcast(roomId);
                        log.info("[웹소켓 인터셉터] 유저 {}번 채팅방 세션 제거 완료 (주소 역추적: {})", userId, destination);
                    } catch (NumberFormatException e) {
                        log.error("[웹소켓 인터셉터] UNSUBSCRIBE 방 ID 파싱 실패: {}", destination);
                    }
                }
                // 🔔 알림 채널 구독 해제 (이제 완벽하게 매칭되어 정상 작동함!)
                else if (destination.contains("/notice/total-counts") || destination.contains("/total-counts")) {
                    notificationSessionManager.leaveNotificationChannel(userId, sessionId);
                    log.info("[웹소켓 인터셉터] 유저 {}번 실시간 알림 채널 세션 제거 완료 (주소 역추적: {})", userId, destination);
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
                // 연결 종료 전, 이 유저가 타이핑 중이던 방 목록을 먼저 확보
                Set<Long> typingRoomIds = typingSessionManager.getTypingRooms(userId);

                // 연결이 완전히 끊기는 것은 방을 나가는 것이 맞으므로 무조건 제거
                sessionManager.leaveRoom(userId);

                notificationSessionManager.leaveNotificationChannel(userId, accessor.getSessionId());
                typingSessionManager.clearUser(userId);

                // 정리 후, 영향받은 각 방에 갱신된 타이핑 상태 브로드캐스트
                for (Long roomId : typingRoomIds) {
                    typingBroadcasterProvider.getObject().broadcast(roomId);
                }

                // 해당 세션의 모든 장부 기록 싹 청소
                if (sessionId != null) {
                    subscriptionRegistry.keySet().removeIf(key -> key.startsWith(sessionId + "_"));
                }
                log.info("[웹소켓 인터셉터] 유저 {}번의 웹소켓 연결이 종료되어 세션에서 완전히 제거되었습니다.", userId);
            }
        }
        return message;
    }

    //JWT 토큰 기반의 시큐리티 컨텍스트에서 유저ID 추출하기
    private Long getUserIdFromAccessor(StompHeaderAccessor accessor) {
        // 1차: 타 담당자가 CONNECT 시점에 이미 저장해 둔 Security Context(Principal) 활용
        if (accessor.getUser() != null) {
            try {
                // 수강 인터셉터 규격 그대로 형변환하여 userId(Long) 추출
                org.springframework.security.core.Authentication authentication =
                        (org.springframework.security.core.Authentication) accessor.getUser();
                com.wanted.momocity.auth.infrastructure.security.CustomUserDetails userDetails =
                        (com.wanted.momocity.auth.infrastructure.security.CustomUserDetails) authentication.getPrincipal();
                return userDetails.getUserId(); // 🎯 깔끔하게 ID 반환!
            } catch (Exception e) {
                log.warn("[웹소켓 인터셉터] Principal 객체에서 유저 ID 추출 실패: {}", e.getMessage());
            }
        }

        // 2차: 타 담당자가 세션 Attributes에 심어둔 "userId"가 있는지 확인
        if (accessor.getSessionAttributes() != null && accessor.getSessionAttributes().containsKey("userId")) {
            return (Long) accessor.getSessionAttributes().get("userId");
        }

        // 3차: SUBSCRIBE 프레임 전송 시 Native Header에 토큰이 들어왔을 경우 직접 파싱
        String authHeader = accessor.getFirstNativeHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();
            try {
                if (jwtTokenProvider.validateToken(token)) {
                    // 수강 인터셉터와 동일하게 Provider를 통해 유저 ID 바로 획득!
                    org.springframework.security.core.Authentication authentication = jwtTokenProvider.getAuthentication(token);
                    com.wanted.momocity.auth.infrastructure.security.CustomUserDetails userDetails =
                            (com.wanted.momocity.auth.infrastructure.security.CustomUserDetails) authentication.getPrincipal();
                    return userDetails.getUserId();
                }
            } catch (Exception e) {
                log.error("[웹소켓 인터셉터] 토큰 직접 검증 및 유저 ID 파싱 실패: {}", e.getMessage());
            }
        }

        log.warn("[웹소켓 인터셉터] 유효한 인증 토큰이나 세션 유저 정보를 찾을 수 없습니다.");
        return null;
    }
}
