package com.wanted.momocity.chatbot.infrastructure.adapter;

import com.wanted.momocity.chatbot.application.port.GeminiClientPort;
import com.wanted.momocity.chatbot.infrastructure.config.GeminiProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor

/* comment.
    GeminiClientPort 의 실제 구현체 - 제미나이 스트리밍 API 를 WebClient 를 통해서 호출하고,
    청크를 StreamCallback 으로 흘린다.
 */

public class GeminiClientAdapter implements GeminiClientPort {

    private final WebClient geminiWebClient;
    private final GeminiProperties geminiProperties;

    // Gemini 요청 body 를 만들고, .bodyToFlux() 로 SSE 청크를 스트림으로 받는다.
    // 이렇게 청크가 올 때마다, 에러가 날 때마다, 끝날 때 콜백을 실행하게 된다.
    @Override
    public void streamAnswer(String prompt, StreamCallback callback) {
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt))))
        );

        geminiWebClient.post()
                .uri("/v1beta/models/{model}:streamGenerateContent?alt=sse", geminiProperties.getModel())
                .bodyValue(requestBody)
                .retrieve()
                .bodyToFlux(GeminiStreamChunk.class)
                .subscribe(
                        chunk -> extractText(chunk).forEach(callback::onChunk),
                        callback::onError,
                        callback::onComplete
                );
    }

    // Gemini 응답 청크 하나에서 텍스트만 뽑아냄, candidates/content/parts 가 비어있을 수도 있어 방어적으로 처리
    private List<String> extractText(GeminiStreamChunk chunk) {
        // candidates 자체가 비어있으면 텍스트 없음
        if (chunk.candidates() == null || chunk.candidates().isEmpty()) {
            return List.of();
        }
        // finishReason만 있고 content 자체가 없는 종료 청크일 수 있어 null 체크
        GeminiStreamChunk.Content content = chunk.candidates().get(0).content();
        if (content == null || content.parts() == null) {
            return List.of();
        }
        // 텍스트만 꺼내서 리스트로 반환
        return content.parts().stream().map(GeminiStreamChunk.Part::text).filter(text -> text != null).toList();
    }

    // Gemini 스트리밍 응답 JSON 모양 그대로 옮겨 담는 전용 DTO (이 파일 안에서만 씀)
    private record GeminiStreamChunk(List<Candidate> candidates) {
        private record Candidate(Content content) {}
        private record Content(List<Part> parts) {}
        private record Part(String text) {}
    }
}
