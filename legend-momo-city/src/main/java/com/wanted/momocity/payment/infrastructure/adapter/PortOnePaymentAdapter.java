package com.wanted.momocity.payment.infrastructure.adapter;

import com.wanted.momocity.payment.application.port.PortOnePaymentPort;
import com.wanted.momocity.payment.domain.exception.PaymentNotAttemptedException;
import com.wanted.momocity.payment.domain.exception.PortOneApiException;
import com.wanted.momocity.payment.domain.model.PortOnePaymentDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class PortOnePaymentAdapter implements PortOnePaymentPort {
    private final WebClient portOneWebClient;

    @Override
    public PortOnePaymentDetail verifyPayment(String paymentId) {
        Map response;

        try {
            response = portOneWebClient.get()
                    .uri("/payments/{paymentId}", paymentId)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
        } catch (WebClientResponseException.NotFound e) {
            throw new PaymentNotAttemptedException(
                    "결제가 진행되지 않았습니다." // 결제 창 없이 verify 하는 경우
            );
        } catch (WebClientResponseException e) {
            throw new PortOneApiException("포트원 결제 조회 실패: " + e.getStatusCode() + " " + e.getResponseBodyAsString());
        }

        if (response == null) {
            throw new PortOneApiException("포트원 결제 조회 응답이 비어있습니다.");
        }

        String status = (String) response.get("status");
        String pgTxId = (String) response.get("pgTxId");

        @SuppressWarnings("unchecked")
        Map<String, Object> amountMap = (Map<String, Object>) response.get("amount");
        Long total = amountMap != null
                ? Long.valueOf(String.valueOf(amountMap.get("total")))
                : null;

        return new PortOnePaymentDetail(paymentId, status, total, pgTxId);
    }

    @Override
    public void cancelPayment(String paymentId, String reason) {
        portOneWebClient.post()
                // 포트원이 만들어둔 엔드포인트 - 우리가 요청하면 알아서 취소해줌
                .uri("/payments/{paymentId}/cancel", paymentId)
                .bodyValue(Map.of("reason", reason))
                .retrieve()
                .toBodilessEntity()
                .block();
    }
    /*comment
    *  1. 우리 서버가 cancelPayment() 메서드를 호출
    *  2. 그 메서드 안에서 WebClient가 실제로 https://api.portone.io/payments/{paymentId}/cancel에 POST 요청을 네트워크로 전송
    *  3. 포트원 서버가 그 요청을 받아서 실제로 취소 처리
    *  4. 포트원이 응답(성공/실패)을 돌려줌
    *  5. 우리 서버가 그 응답을 받아서 처리*/
}
