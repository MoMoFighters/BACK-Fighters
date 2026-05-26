package com.wanted.momocity.viewing.infrastructure.adapter;

import com.wanted.momocity.viewing.application.port.S3Port;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.time.Duration;

/*
* comment.
*  S3PresignedUrlAdapter
*  - S3Port 인터페이스 구현체
*  - AWS S3 SDK 를 직접 다루는 유일한 클래스
*  - Application 은 S3Port 인터페이스만 알고 이 클래스를 직접 모름
 * */

@Component
@RequiredArgsConstructor
public class S3PresignedUrlAdapter implements S3Port {

    private final S3Presigner s3Presigner;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Override
    public String generatePresignedUrl(String videoUrl) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(videoUrl)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                // S3 에 저장된 비공개 영상을 인증 없이 일정 시간만 접근 가능한 임시 URL 로 발급
                // 유효시간 1시간 (3600초)
                .signatureDuration(Duration.ofHours(1))
                .getObjectRequest(getObjectRequest)
                .build();

        return s3Presigner.presignGetObject(presignRequest)
                .url()
                .toString();
    }
}
