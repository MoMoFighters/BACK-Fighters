package com.wanted.momocity.chatbot.infrastructure.adapter;

import com.wanted.momocity.chatbot.application.port.GeminiClientPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

// 기존 WebClientConfig 패턴 재사용 (OAuthClient들과 동일)
@Component
@RequiredArgsConstructor
public class GeminiClientAdapter implements GeminiClientPort {

    private final WebClient webClient;
}
