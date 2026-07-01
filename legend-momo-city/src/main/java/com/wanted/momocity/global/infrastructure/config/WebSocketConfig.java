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

    private final TopicSubscriptionInterceptor subscriptionInterceptor;
    private final ViewingProgressChannelInterceptor viewingProgressChannelInterceptor;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        //프론트엔드가 웹소켓 연결을 처음 맺을 주소
        registry.addEndpoint("/ws-chat")
                .setAllowedOriginPatterns("http://localhost:4444", "https://momocity-six.vercel.app")
                .withSockJS();
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
                viewingProgressChannelInterceptor
        );
    }
}
