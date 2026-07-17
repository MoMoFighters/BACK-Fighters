package com.wanted.momocity.global.infrastructure.config;

import com.wanted.momocity.chatbot.infrastructure.config.GeminiProperties;
import com.wanted.momocity.chatbot.infrastructure.config.PolicySearchProperties;
import com.wanted.momocity.payment.infrastructure.portone.PortOneProperties;
import io.netty.channel.ChannelOption;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(
        {PortOneProperties.class, PolicySearchProperties.class, GeminiProperties.class})

/* comment.
    momo-ai 서비스 를 보낼 전용 WebClient 빈을 추가한다.
 */

public class WebClientConfig {

    @Bean
    public WebClient webClient() {
        return WebClient.builder().build();
    }

    // PortOne API 전용 WebClient -PortOneProperties 빈 주입
    @Bean
    public WebClient portOneWebClient(PortOneProperties props) {
        HttpClient httpClient = HttpClient.create()
                // 요청을 보내고 응답을 기다리는 최대 시간 - 5초 안에 응답 없으면 타임아웃 예외 발생
                // → PortOne 서버가 안 죽어도 응답이 느리면, 우리 서버가 무한정 붙잡혀있는 걸 방지
                .responseTimeout(Duration.ofSeconds(5))
                // TCP 커넥션 자체를 맺는 데 걸리는 최대 시간 (3초). 네트워크 단절 시 여기서 먼저 끊김
                // responseTimeout과는 다른 단계 - "연결 자체"가 안 되는 상황을 방지
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000);

        return WebClient.builder()
                // 매 요청마다 전체 URL 안 써도 되게, PortOne API의 기본 도메인을 미리 지정
                // 예: props.getBaseUrl()이 "https://api.portone.io"면
                // 이후 webClient.get().uri("/payments/{id}") 처럼 경로만 쓰면 됨

                .baseUrl(props.getBaseUrl())

                // 위에서 만든 httpClient(타임아웃 설정 포함)를 WebClient에 연결
                // 이 설정을 안 하면 WebClient는 기본 커넥터를 쓰고, 우리가 설정한 타임아웃이 적용 안 됨
                .clientConnector(new ReactorClientHttpConnector(httpClient))

                // 이 WebClient로 나가는 모든 요청에 자동으로 붙는 인증 헤더
                // PortOne API 인증 방식이 "Authorization: PortOne {API_SECRET}" 형태인 걸로 보임
                .defaultHeader(HttpHeaders.AUTHORIZATION, "PortOne " + props.getApiSecret())
                .build();
    }
    // momo-ai(Chroma+Gemini RAG) 서비스 전용 WebClient - PolicySearchProperties 빈 주입
    @Bean
    public WebClient policySearchWebClient(PolicySearchProperties props) {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(5))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000);

        return WebClient.builder()
                .baseUrl(props.getBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    // Gemini 스트리밍 API 전용 WebClient - GeminiProperties 빈 주입
    @Bean
    public WebClient geminiWebClient(GeminiProperties props) {
        HttpClient httpClient = HttpClient.create()
                // 스트리밍 응답이라 5초로는 부족함 - SSE 전체 타임아웃(60초) 기준에 맞춤
                .responseTimeout(Duration.ofSeconds(60))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000);

        return WebClient.builder()
                // Gemini API 기본 도메인은 환경별로 안 바뀌는 고정값이라 yaml 없이 하드코딩
                // 현재 Module05 에서는 Gemini 모델만 사용할꺼라 이렇게 판단해서 넣었음.
                .baseUrl("https://generativelanguage.googleapis.com")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader("x-goog-api-key", props.getApiKey())
                .build();
    }

}
