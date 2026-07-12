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
        /*comment
        *  포트원 api의 응답 구조
        *  "status" : "결제 상태",
        *  "pgTxId" : "포트원이 발급해주는 고유 번호",
        *  "amount" : "결제된 금액"
        *  이런 식 */

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
        /*comment
        *  amount는 이런 맵현식
        *  "amount": {
        *    "total": 9900,
        *    "taxFree": 0,
        *    "vat": 900
        *  }
        * */
        Long total = null;
        if (amountMap != null && amountMap.get("total") != null) {
            total = Long.valueOf(String.valueOf(amountMap.get("total")));
        }else {
            throw new PortOneApiException("포트원 응답에 금액 정보가 없습니다. status :"+status);
        }

        return new PortOnePaymentDetail(paymentId, status, total, pgTxId);
    }

    @Override
    public void cancelPayment(String paymentId, String reason) {
        try {
            portOneWebClient.post()
                    // 포트원이 만들어둔 엔드포인트 - 우리가 요청하면 알아서 취소해줌
                    .uri("/payments/{paymentId}/cancel", paymentId)
                    .bodyValue(Map.of("reason", reason))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (WebClientResponseException e) {
            throw new PortOneApiException("포트원 취소 실패: " + e.getStatusCode() + " " + e.getResponseBodyAsString());
            /*comment
            *  포트원이 에러 응답을 줄 때 바디에 담아서 보냄
            *  {
            *    "code": "INVALID_PAYMENT",
            *    "message": "이미 취소된 결제입니다."
            *  }
            *  like this
            *  그래서 이거를 꺼냄 -> 디버깅용*/
        }
    }
    /*comment
    *  1. 우리 서버가 cancelPayment() 메서드를 호출
    *  2. 그 메서드 안에서 WebClient가 실제로 https://api.portone.io/payments/{paymentId}/cancel에 POST 요청을 네트워크로 전송
    *  3. 포트원 서버가 그 요청을 받아서 실제로 취소 처리
    *  4. 포트원이 응답(성공/실패)을 돌려줌
    *  5. 우리 서버가 그 응답을 받아서 처리*/
}
