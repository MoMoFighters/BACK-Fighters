package com.wanted.momocity.global.infrastructure.s3;

import com.wanted.momocity.global.application.s3.S3PresignedUrlPort;
import com.wanted.momocity.global.infrastructure.config.CloudFrontSignedUrlProperties;
import com.wanted.momocity.viewing.application.port.S3Port;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.cloudfront.CloudFrontUtilities;
import software.amazon.awssdk.services.cloudfront.model.CannedSignerRequest;
import software.amazon.awssdk.services.cloudfront.url.SignedUrl;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectTaggingRequest;
import software.amazon.awssdk.services.s3.model.Tagging;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.security.PrivateKey;
import java.time.Duration;
import java.time.Instant;

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
public class S3PresignedUrlAdapter implements S3Port , S3PresignedUrlPort {

    // S3Presigned: AWS SDK 가 제공하는 Presigned URL 생성 전용 객체
    // S3Config 에서 Bean 으로 등록해둔 것을 주입받음
    private final S3Presigner s3Presigner;
    private final S3Client s3Client;

    // 신규 - CloudFront 서명에 필요한 의존성 주입
    private final CloudFrontUtilities cloudFrontUtilities;
    private final PrivateKey cloudFrontPrivateKey;
    private final CloudFrontSignedUrlProperties cloudFrontSignedUrlProperties;

    // application.yaml 에서 버킷 이름 주입
    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    /*
     * comment.
     *  공통 - 전체 URL 또는 key 형태 입력을 받아 순수 S3 key 만 추출
     *  - 레거시 데이터(풀 URL 저장) 와 현재 데이터(key 만 저장) 둘 다 대응
     *  - generatePresignedUrl(), generateCloudFrontSignedUrl() 양쪽에서 공용으로 사용
     */
    private String extractKey(String urlOrKey) {
        return urlOrKey.contains(".amazonaws.com/")
                ? urlOrKey.substring(urlOrKey.indexOf(".amazonaws.com/") + ".amazonaws.com/".length())
                : urlOrKey;
    }

    @Override
    public String generatePresignedUrl(String videoUrl) {

        String key = extractKey(videoUrl);
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

    /*
     * comment.
     *  신규 - CloudFront Signed URL 발급
     *  - videoUrl 은 DB 에 key 형태로만 저장되어 있음 (ex. lectures/1/chapters/1/chapter01.mp4)
     *  - 별도 파싱 없이 바로 CloudFront resourceUrl 조합에 사용
     */
    @Override
    public String generateCloudFrontSignedUrl(String videoUrl) {

        // 레거시 풀 URL이 들어와도 key 만 추출해서 사용 (generatePresignedUrl 과 동일 로직 재사용)
        String key = extractKey(videoUrl);

        // CloudFront 도메인 + key 조합 -> 서명 대상 리소스 URL
        String resourceUrl = "https://" + cloudFrontSignedUrlProperties.domain() + "/" + videoUrl;

        // CannedSignerRequest: 단순 만료시간 기반 서명 (IP 제한 등 없는 기본형)
        CannedSignerRequest request = CannedSignerRequest.builder()
                .resourceUrl(resourceUrl)
                .privateKey(cloudFrontPrivateKey)
                .keyPairId(cloudFrontSignedUrlProperties.keyPairId())
                .expirationDate(Instant.now().plusSeconds(cloudFrontSignedUrlProperties.expirationSeconds()))
                .build();

        SignedUrl signedUrl = cloudFrontUtilities.getSignedUrlWithCannedPolicy(request);

        log.info("[CloudFront] Signed URL 발급 완료 | key={}", videoUrl);

        return signedUrl.url();
    }

    /*
     * comment.
     *  [역할]
     *  S3 객체에 archive=true 태그 부착
     *  -> momocity-media 버킷의 lectures-archive-lifecycle 규칙(prefix=lectures/ + tag=archive:true)이
     *    이 태그를 감지해서 30일 후 Glacier, 4년 후 Deep Archive로 자동 전환
     */

    public void markAsArchive(String s3Key) {
        Tag archiveTag = Tag.builder()
                .key("archive")
                .value("true")
                .build();

        Tagging tagging = Tagging.builder()
                .tagSet(archiveTag)
                .build();

        PutObjectTaggingRequest request = PutObjectTaggingRequest.builder()
                .bucket(bucketName)   // 기존 필드명과 다르면 맞춰서 수정 필요
                .key(s3Key)
                .tagging(tagging)
                .build();

        s3Client.putObjectTagging(request);   // 기존 s3Client 필드 재사용
    }

}
