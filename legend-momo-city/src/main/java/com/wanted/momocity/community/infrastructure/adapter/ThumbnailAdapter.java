package com.wanted.momocity.community.infrastructure.adapter;

import com.wanted.momocity.community.application.post.port.ThumbnailPort;
import com.wanted.momocity.global.application.s3.S3UploadPort;
import com.wanted.momocity.global.infrastructure.cloudfront.CloudFrontUrlConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.UUID;

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

    @Override
    public String generateThumbnail(String originalImageUrl) {
        try {
            // 1. 원본 이미지 다운로드 (URL 스트림으로 바로 읽음)
            URL url = new URL(originalImageUrl);

            // 2. Thumbnailator 로 리사이징
            //    - size(400, 400) : 정사각형 목표 크기
            //    - crop(Positions.CENTER) : 원본 비율이 다르면 중앙 기준으로 잘라서 정사각형 맞춤
            //    - outputQuality(0.8f) : 화질 80%
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Thumbnails.of(url)
                    .size(THUMBNAIL_SIZE, THUMBNAIL_SIZE)
                    .crop(net.coobird.thumbnailator.geometry.Positions.CENTER)
                    .outputQuality(THUMBNAIL_QUALITY)
                    .outputFormat("jpg")
                    .toOutputStream(outputStream);

            byte[] thumbnailBytes = outputStream.toByteArray();

            // 3. byte[] -> MultipartFile 변환
            String filename = UUID.randomUUID() + "_thumb.jpg";
            ByteArrayMultipartFile multipartFile = new ByteArrayMultipartFile(
                    "file", filename, "image/jpeg", thumbnailBytes
            );

            // 4. S3 업로드 (key 반환) -> CloudFront URL 변환
            //    -> 기존 uploadImage() 와 완전히 동일한 흐름
            String key = s3UploadPort.upload(multipartFile, THUMBNAIL_FOLDER);
            String thumbnailUrl = cloudFrontUrlConverter.convert(key);

            log.info("[Thumbnail] 생성 완료 | original={}, thumbnail={}", originalImageUrl, thumbnailUrl);

            return thumbnailUrl;

        } catch (IOException e) {
            log.error("[Thumbnail] 생성 실패 | original={}, error={}", originalImageUrl, e.getMessage());
            // 실패 시 원본 URL 유지 (썸네일 실패가 게시글 자체를 막으면 안 됨)
            return originalImageUrl;
        }
    }

}
