package com.wanted.momocity.chatbot.infrastructure.config;

/* comment.
    application-ai.yaml 파일과 gemini.api.key, gemini.model 값을 자바 객체로 바인딩해준다.
 */

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gemini")
@Getter
public class GeminiProperties {

    // api-key 가 자동으로 apiKey 에 바인딩 되는 건 스프링의 스네이크 케이스 -> 카멜케이스 변환으로 인해서 생긴다.
    // PortOneProperties.apiSecret 과 같은 방식이다.

    private String apiKey;
    private String model;

    public GeminiProperties(String apiKey, String model) {
        this.apiKey = apiKey;
        this.model = model;
    }

}
