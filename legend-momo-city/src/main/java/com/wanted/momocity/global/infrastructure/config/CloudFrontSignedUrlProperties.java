package com.wanted.momocity.global.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/*
 * comment.
 *  application.yml 의 cloudfront.* 설정값을 바인딩하는 클래스
 *  - domain : CloudFront 배포 도메인 (ex. d1w7ptjpsyo7f4.cloudfront.net)
 *  - keyPairId : CloudFront Public Key 등록 시 발급받는 ID
 *  - privateKeyPath : 서명용 private key 파일 경로 (환경변수로 분리 권장)
 *  - expirationSeconds : Signed URL 유효시간 (기존 3600초와 동일하게 시작)
 */

@ConfigurationProperties(prefix = "cloudfront")
public record CloudFrontSignedUrlProperties(
        String domain,
        String keyPairId,
        String privateKeyPath,
        int expirationSeconds
) {
}
