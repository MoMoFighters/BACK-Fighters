package com.wanted.momocity.viewing.infrastructure.adapter;

import com.wanted.momocity.viewing.application.port.S3Port;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class S3PresignedUrlAdapter implements S3Port {

    // S3Presigner: AWS SDK 가 제공하는 Presigned URL 생성 전용 객체
    // S3Config 에서 Bean 으로 등록해둔 것을 주입받음
    private final S3Presigner s3Presigner;

    // application.yaml 에서 버킷 이름 주입
    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    /*
    * comment.
    *  전체 URL → key 추출
    *  S3 에 파일 접근할 때 필요한 것은 전체 URL 이 아니라 버킷 내 파일 경로 (key)
    *  - contains(".amazonaws.com/") 로 전체 URL 인지 확인
    *  -> 맞으면 ".amazonaws.com/" 이후 부분만 추출, 아니면 이미 key 형태라 그대로 사용
     * */

    @Override
    public String generatePresignedUrl(String videoUrl) {

        String key = videoUrl.contains(".amazonaws.com/")
                ? videoUrl.substring(videoUrl.indexOf(".amazonaws.com/") + ".amazonaws.com/".length())
                : videoUrl;
        log.info("[S3] key = {}", key);

        // GetObjectRequest: S3 에서 파일을 가져오기 위한 요청 객체
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                // videoUrl 대신 key 사용
                .key(key)
                .build();

        // GetObjectPresignRequest: Presigned URL 생성 요청 객체
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                // S3 에 저장된 비공개 영상을 인증 없이 일정 시간만 접근 가능한 임시 URL 로 발급
                // 유효시간 1시간 (3600초)
                .signatureDuration(Duration.ofHours(1))
                .getObjectRequest(getObjectRequest)
                .build();

        // s3Presigner.presignGetObject(): 실제 Presigned URL 생성
        return s3Presigner.presignGetObject(presignRequest)
                // .url().toString(): URL 객체를 문자열로 변환해서 반환
                .url()
                .toString();
    }
}
