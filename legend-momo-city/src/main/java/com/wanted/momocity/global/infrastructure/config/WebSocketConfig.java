package com.wanted.momocity.global.infrastructure.config;

import com.wanted.momocity.viewing.infrastructure.stomp.ViewingProgressChannelInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@RequiredArgsConstructor
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final String[] ALLOWED_ORIGIN_PATTERNS = {
            "http://localhost:3000",
            "http://localhost:4444",
            "https://momocity-six.vercel.app"
//            "https://*.vercel.app",
//            "https://*.ngrok-free.dev"
    };

    private final TopicSubscriptionInterceptor subscriptionInterceptor;
    private final ViewingProgressChannelInterceptor viewingProgressChannelInterceptor;
    private final StudyStompInterceptor studyStompInterceptor;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        //프론트엔드가 웹소켓 연결을 처음 맺을 주소
        registry.addEndpoint("/ws-chat")
                .setAllowedOriginPatterns(ALLOWED_ORIGIN_PATTERNS);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        //내장 브로커가 메시지를 구독자(유저들)에게 전달할 때 사용할 접두사
        registry.enableSimpleBroker("/sub");
        //프론트엔드가 서버로 메시지를 보낼 때 사용할 접두사
        registry.setApplicationDestinationPrefixes("/pub");
        //개별 사용자 전용 메시지 라우팅을 위한 접두사 지정
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        //프론트에서 들어오는 신호 길목에 인터셉터 장착
        registration.interceptors(
                subscriptionInterceptor,
                // 수강 페이지 STOMP JWT 검증 인터셉터
                viewingProgressChannelInterceptor,
                // study(열품타) 그룹방 구독 시 멤버십 검증 인터셉터
                // subscriptionInterceptor가 먼저 CONNECT 인증을 끝내야
                // 이 인터셉터가 Principal/세션 attributes를 읽을 수 있으므로 반드시 뒤에 위치
                studyStompInterceptor
        );
    }

}