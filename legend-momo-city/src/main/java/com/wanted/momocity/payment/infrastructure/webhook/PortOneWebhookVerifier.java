package com.wanted.momocity.payment.infrastructure.webhook;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Component
public class PortOneWebhookVerifier {

    // HMAC 계산 알고리즘 -> 이 방식으로 계산하겠다는 고정값
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    // PortOne 콘솔에서 발급하는 웹훅 시크릿
    private static final String SECRET_PREFIX = "whsec_";

    private final byte[] secretBytes;

    /*comment
     * PortOne이 보낸 웹훅 요청이 진짜 PortOne이 보낸 게 맞는지 검증
     */

    public PortOneWebhookVerifier(@Value("${portone.webhook-secret}") String secret) {
        String raw = secret.startsWith(SECRET_PREFIX) ? secret.substring(SECRET_PREFIX.length()) : secret;
        this.secretBytes = Base64.getDecoder().decode(raw);
    }

    /*comment*
     * 웹훅 요청이 진짜 PortOne이 보낸 게 맞는지 확인
     * PaymentWebhookController가 요청을 받자마자 제일 먼저 호출
     * - webhookId : PortOne이 보낸 요청 식별자
     * - webhookTimestamp : PortOne이 요청 보낸 시각
     * - rawBody : 요청 본문 원본 문자열
     * - webhookSignatureHeader PortOne이 보낸 서명값
     *  일치하면 true 다르면 false
     */
    public boolean verify(String webhookId, String webhookTimestamp, String rawBody, String webhookSignatureHeader) {
        String signedContent = webhookId + "." + webhookTimestamp + "." + rawBody;
        String expected = sign(signedContent);

        for (String candidate : webhookSignatureHeader.split(" ")) {
            String[] parts = candidate.split(",", 2);
            if (parts.length == 2 && constantTimeEquals(parts[1], expected)) {
                return true;
            }
        }
        return false;
    }

    private boolean constantTimeEquals(String a, String b) {
        return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }
    /*comment
    *  .equals()랑 다르게 MessageDigest.isEqual()은 끝까지 다 비교하고 나서 답을 줌
    *  */

    private String sign(String content) {
        try {
            // HMAC-SHA256 방식의 계산기 인스턴스 생성
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secretBytes, HMAC_ALGORITHM));

            // 실제 계산 수행
            byte[] hash = mac.doFinal(content.getBytes(StandardCharsets.UTF_8));

            // 계산 결과가 바이트라 그대로 보기 힘드니까 문자열로 변환
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new IllegalStateException("웹훅 서명 생성 실패", e);
        }
    }
}
