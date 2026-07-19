package com.wanted.momocity.payment.presentation.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wanted.momocity.payment.application.command.WebhookCommand;
import com.wanted.momocity.payment.application.usecase.PaymentWebhookUseCase;
import com.wanted.momocity.payment.infrastructure.webhook.PortOneWebhookVerifier;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v3/payment")
@Tag(name="Payment - 웹훅 결제 상태 관리")
public class PaymentWebhookController {
    private final PortOneWebhookVerifier webhookVerifier;
    private final PaymentWebhookUseCase paymentWebhookUseCase;
    private final ObjectMapper objectMapper;
    // JSON ↔ 자바 객체 변환기

    /*comment
    *  기본적으로 서버 - 서버 통신
    *  portone 서버가 헤어에 webhook-id, webhook-timestamp, webhook-signature 이 셋을 헤더에 붙여서 보냄
    *  이떼 포트원 콘솔에서 발급한 키+헤더에서 받아 계산한 검증값이랑
    *  포트원이 키로 생성해낸 검증값이 같은 비교 -> 위조 방지 */

    /*comment
    *  이 겅증값을 json으로 파싱하면 파싱된 객체를 다시 직렬화해서 비교하면
    *  값이 미세하게 달라져서 서명이 안 맞을 수도 있어서 원본 문자열을 그대로 받고
    *  그 후에 별도로 파싱 작업 진행 */

    /*comment
    *  <전체 구조>
    *  - PortOne 서버 ──(POST, 헤더 포함)──> 백엔드
    *  - webhook-id, webhook-timestamp, webhook-signature는 PortOne 서버가 요청을 보낼 때 자기가 직접 채워서 보내는 헤더
    *  - 결제 상태가 바뀌면 PortOne이 우리 서버 웹훅 URL로 자기가 직접 헤더에 실어 보냄 !! */

    @PostMapping("/webhook")
    public ResponseEntity<Void> receive(
            @RequestHeader("webhook-id") String webhookId,
            @RequestHeader("webhook-timestamp") String webhookTimestamp,
            @RequestHeader("webhook-signature") String webhookSignature,
            @RequestBody String rawBody
    ) {
        // 실제 검증값 / 포트원의 검증값이 같은지 검증
        if (!webhookVerifier.verify(webhookId, webhookTimestamp, rawBody, webhookSignature)) {
            log.warn("[webhook] 서명 검증 실패 webhookId={}", webhookId);
            return ResponseEntity.status(401).build();
        }

        /*comment
        *  Jackson 라이브러리
        *  JSON 문자열을 자바 객체로 */
        JsonNode event = parse(rawBody);
        String eventType = event.get("type").asText();
        String paymentId = event.path("data").path("paymentId").asText();
        /*comment
        *  rawBody - String - {"type":"Transaction.Paid","data":{...}}
        *  event - JsonNode - 파싱된 JSON 트리 전체
        *  type - String - "Transaction.Paid"
        *  paymentId - String - "paymentId" */

        paymentWebhookUseCase.handle(new WebhookCommand(eventType, paymentId));

        return ResponseEntity.ok().build();
    }

    private JsonNode parse(String rawBody) {
        try {
            return objectMapper.readTree(rawBody);
            /*comment
            *  -readTree()
            *  타입 없이 트리 구조로만 파싱*/
        } catch (Exception e) {
            throw new IllegalArgumentException("웹훅 payload 파싱 실패", e);
        }
    }


}
