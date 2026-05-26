package com.wanted.momocity.lecture.infrastructure.adapter;

import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.momocity.lecture.application.command.LectureThumbnailFile;
import com.wanted.momocity.lecture.application.port.ThumbnailStoragePort;
import org.springframework.stereotype.Component;

import java.util.UUID;

/*
 * S3ThumbnailStorageAdapter는 강의 썸네일 파일을 S3에 업로드하는 adapter
 */
@Component
public class S3ThumbnailStorageAdapter implements ThumbnailStoragePort {

    private static final long MAX_THUMBNAIL_SIZE = 5 * 1024 * 1024;
    private static final String TEMP_THUMBNAIL_BASE_URL = "https://example.com/images/";

    @Override
    public String upload(LectureThumbnailFile thumbnailFile) {
        validateThumbnailFile(thumbnailFile);

        // 파일명 충돌을 막기 위해 UUID 기반 저장명을 만든다.
        String storedFilename = UUID.randomUUID() + "-" + thumbnailFile.originalFilename();

        /*
         * 실제 S3 연동 전까지는 임시 URL을 반환한다.
         * 추후 S3 업로드 결과 URL로 교체하면 된다.
         */
        return TEMP_THUMBNAIL_BASE_URL + storedFilename;
    }

    /*
     * 썸네일 파일의 기본 조건을 검증
     * 명세 기준 썸네일 최대 크기는 5MB다.
     */
    private void validateThumbnailFile(LectureThumbnailFile thumbnailFile) {
        if (thumbnailFile == null || thumbnailFile.content() == null || thumbnailFile.content().length == 0) {
            throw new DomainRuleViolationException("썸네일 이미지는 필수입니다.");
        }

        if (thumbnailFile.size() > MAX_THUMBNAIL_SIZE) {
            throw new DomainRuleViolationException("썸네일 파일의 최대 크기는 5MB입니다.");
        }

        if (!isImageContentType(thumbnailFile.contentType())) {
            throw new DomainRuleViolationException("썸네일은 이미지 파일만 등록할 수 있습니다.");
        }
    }

    // 허용할 이미지 Content-Type을 확인한다.
    private boolean isImageContentType(String contentType) {
        return "image/jpeg".equals(contentType)
                || "image/png".equals(contentType)
                || "image/webp".equals(contentType);
    }
}