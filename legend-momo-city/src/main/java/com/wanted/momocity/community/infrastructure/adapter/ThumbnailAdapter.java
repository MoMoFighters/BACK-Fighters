package com.wanted.momocity.community.infrastructure.adapter;

import com.wanted.momocity.community.application.post.port.ThumbnailPort;
import com.wanted.momocity.global.application.s3.S3UploadPort;
import com.wanted.momocity.global.infrastructure.cloudfront.CloudFrontUrlConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.UUID;

/*
 * comment.
 *  ThumbnailAdapter
 *  - 원본 이미지 다운로드 -> Thumbnailator 로 리사이징 -> S3 재업로드
 *  - 크기 400x400 (정사각형, 중앙 기준 crop), 화질 80%
 *  -
 *  [보안 강화]
 *  - ALLOWED_HOSTS 화이트리스트로 SSRF 방지
 *    (서비스에서 사용하는 S3/CloudFront 도메인 외에는 요청 자체를 거부)
 *  - MAX_DOWNLOAD_BYTES 로 스트림 크기 제한
 *    (비정상적으로 큰 이미지가 들어와도 메모리 고갈 방지)
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class ThumbnailAdapter implements ThumbnailPort {

    // 기존 S3UploadPort 재사용 - 업로드는 이미 있는 로직 그대로 씀
    private final S3UploadPort s3UploadPort;
    private final CloudFrontUrlConverter cloudFrontUrlConverter;

    private static final int THUMBNAIL_SIZE = 400;
    private static final float THUMBNAIL_QUALITY = 0.8f;

    // S3UploadPort.upload() 가 folder 인자를 받으므로,
    // 기존 업로드 구조와 맞춰 community/thumbnails 를 폴더로 지정
    private static final String THUMBNAIL_FOLDER = "community/thumbnails";

    // 원본 이미지를 내려받을 수 있는 허용 도메인 (SSRF 방지용 화이트리스트)
    // - 우리 서비스 S3/CloudFront 도메인만 허용, 그 외 호스트는 전부 거부
    private static final List<String> ALLOWED_HOSTS = List.of(
            "d1w7ptjpsyo7f4.cloudfront.net",   // (미사용 예정이지만 과거 데이터 호환 위해 유지)
            "d2anv5bir30ioa.cloudfront.net",   // 커뮤니티 이미지 CloudFront 도메인
            "momocity-bucket.s3.ap-northeast-2.amazonaws.com",
            "momocity-media.s3.ap-northeast-2.amazonaws.com"
    );

    // 원본 다운로드 최대 허용 크기 (10MB) - 이 이상이면 리사이징 시도 안 하고 원본 URL 유지
    private static final long MAX_DOWNLOAD_BYTES = 10 * 1024 * 1024;

    @Override
    public String generateThumbnail(String originalImageUrl) {
        try {
            // 1. 원본 이미지 다운로드 (URL 스트림으로 바로 읽음)
            URI uri = new URI(originalImageUrl);

            // 프로토콜 검증 (http/https 만 허용)
            String scheme = uri.getScheme();
            if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
                log.warn("[Thumbnail] 허용되지 않은 프로토콜 요청 차단 | scheme={}", scheme);
                return originalImageUrl;
            }

            // 호스트 화이트리스트 검증
            String host = uri.getHost();
            if (host == null || ALLOWED_HOSTS.stream().noneMatch(host::equalsIgnoreCase)) {
                log.warn("[Thumbnail] 허용되지 않은 호스트 요청 차단 | host={}", host);
                return originalImageUrl;   // 검증 실패 시 원본 URL 그대로 유지 (리사이징만 스킵)
            }

            URL url = uri.toURL();

            // 2. 다운로드 크기 제한 확인 (Content-Length 헤더 우선 체크)
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(10000);
            connection.setRequestMethod("GET");

            // 자동 리다이렉트 비활성화
            // 화이트리스트 호스트가 3xx 로 다른(악성) 호스트로 리다이렉트시켜도 따라가지 않도록 차단
            connection.setInstanceFollowRedirects(false);

            long contentLength = connection.getContentLengthLong();
            if (contentLength > MAX_DOWNLOAD_BYTES) {
                log.warn("[Thumbnail] 원본 이미지 크기 초과로 리사이징 스킵 | url={}, size={}",
                        originalImageUrl, contentLength);
                connection.disconnect();
                return originalImageUrl;
            }

            // 3. 스트림도 이중으로 바운드 처리 (Content-Length 헤더가 없거나 조작된 경우 대비)
            byte[] boundedBytes;
            try (InputStream rawStream = connection.getInputStream()) {
                boundedBytes = readBounded(rawStream, MAX_DOWNLOAD_BYTES);
            } finally {
                connection.disconnect();
            }

            if (boundedBytes == null) {
                log.warn("[Thumbnail] 스트림 크기 초과로 리사이징 중단 | url={}", originalImageUrl);
                return originalImageUrl;
            }

            // 4. Thumbnailator 로 리사이징
            //    - size(400, 400) : 정사각형 목표 크기
            //    - crop(Positions.CENTER) : 원본 비율이 다르면 중앙 기준으로 잘라서 정사각형 맞춤
            //    - outputQuality(0.8f) : 화질 80%
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Thumbnails.of(new java.io.ByteArrayInputStream(boundedBytes))
                    .size(THUMBNAIL_SIZE, THUMBNAIL_SIZE)
                    .crop(Positions.CENTER)
                    .outputQuality(THUMBNAIL_QUALITY)
                    .outputFormat("jpg")
                    .toOutputStream(outputStream);

            byte[] thumbnailBytes = outputStream.toByteArray();

            // 5. byte[] -> MultipartFile 변환 (S3 재업로드)
            String filename = UUID.randomUUID() + "_thumb.jpg";
            ByteArrayMultipartFile multipartFile = new ByteArrayMultipartFile(
                    "file", filename, "image/jpeg", thumbnailBytes
            );

            // 6. S3 업로드 (key 반환) -> CloudFront URL 변환
            //    -> 기존 uploadImage() 와 완전히 동일한 흐름
            String key = s3UploadPort.upload(multipartFile, THUMBNAIL_FOLDER);
            String thumbnailUrl = cloudFrontUrlConverter.convert(key);

            log.info("[Thumbnail] 생성 완료 | original={}, thumbnail={}", originalImageUrl, thumbnailUrl);

            return thumbnailUrl;

        } catch (Exception e) {
            log.error("[Thumbnail] 생성 실패 | original={}, error={}", originalImageUrl, e.getMessage());
            // 실패 시 원본 URL 유지 (썸네일 실패가 게시글 자체를 막으면 안 됨)
            return originalImageUrl;
        }

    }

    /*
     * comment.
     *  스트림을 최대 maxBytes 까지만 읽고, 초과 시 null 반환
     *  - Content-Length 헤더가 없거나 신뢰할 수 없는 경우를 대비한 이중 방어
     */
    private byte[] readBounded(InputStream inputStream, long maxBytes) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] chunk = new byte[8192];
        long totalRead = 0;
        int bytesRead;

        while ((bytesRead = inputStream.read(chunk)) != -1) {
            totalRead += bytesRead;
            if (totalRead > maxBytes) {
                return null;   // 제한 초과 -> 즉시 중단
            }
            buffer.write(chunk, 0, bytesRead);
        }

        return buffer.toByteArray();
    }

}
