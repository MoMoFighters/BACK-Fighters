package com.wanted.momocity.chatbot.infrastructure.adapter;

import com.wanted.momocity.chatbot.application.port.PolicySearchPort;
import com.wanted.momocity.chatbot.domain.exception.PolicySearchException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;

/* comment.
    PolicySearchPort 의 실제 구현체. policySearchWebClient 로 FastAPI /search 엔드포인트
    를 호출해서 결과를 받아온다.
 */

@Component
@RequiredArgsConstructor
public class PolicySearchAdapter implements PolicySearchPort {

    private final WebClient policySearchWebClient;

    // 파이썬 SearchRequest 스키마(query, top_k) 에 맞춘 JSON
    // 키 이름이 정확히 "top_k" 여야 한다. Pydantic 필드명 그대로 매칭
    @Override
    @SuppressWarnings("unchecked")
    public List<String> search(String query) {
        Map response;

        try {
            response = policySearchWebClient.post()
                    .uri("/search")
                    .bodyValue(Map.of("query", query, "top_k", 8))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
        } catch (WebClientResponseException e) {
            throw new PolicySearchException(
                    "정책 검색 실패: " + e.getStatusCode() + " " + e.getResponseBodyAsString());
        } catch (WebClientRequestException e) {
            throw new PolicySearchException("momo-ai 서버 연결 실패: " + e.getMessage());
        }

        // 검색 결과가 없을 때의 예외처리
        if (response == null) {
            throw new PolicySearchException("정책 검색 응답이 비어있습니다.");
        }

        List<String> results = (List<String>) response.get("results");
        return results != null ? results : List.of();
    }
}