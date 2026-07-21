package com.wanted.momocity.global.infrastructure.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.cloudfront.CloudFrontUtilities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(CloudFrontProperties.class)
public class CloudFrontConfig {

    private final CloudFrontProperties cloudFrontProperties;

    // CloudFrontUtilities: AWS SDK 가 제공하는 서명 URL 생성 전용 객체
    @Bean
    public CloudFrontUtilities cloudFrontUtilities() {
        return CloudFrontUtilities.create();
    }

    // PrivateKey Bean 등록
    // - PEM 형식 private key 파일을 읽어서 PrivateKey 객체로 변환
    @Bean
    public PrivateKey cloudFrontPrivateKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {

        String pem = Files.readString(Path.of(cloudFrontProperties.privateKeyPath()));

        // PEM 헤더/푸터, 개행 제거 후 Base64 디코딩
        String privateKeyPem = pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decoded = Base64.getDecoder().decode(privateKeyPem);

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        log.info("[CloudFront] Private Key 로드 완료");

        return keyFactory.generatePrivate(keySpec);
    }
}
