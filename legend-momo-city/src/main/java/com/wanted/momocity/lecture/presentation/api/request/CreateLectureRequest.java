package com.wanted.momocity.lecture.presentation.api.request;

import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.momocity.lecture.application.command.CreateLectureCommand;
import com.wanted.momocity.lecture.domain.model.LectureCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;


// CreateLectureRequest는 multipart/form-data 요청을 받는 DTO
public record CreateLectureRequest(

        @NotBlank(message = "강의 제목은 필수입니다.")
        String title,

        @NotBlank(message = "강의 설명은 필수입니다.")
        String description,

        @NotBlank(message = "강의 카테고리는 필수입니다.")
        String category,

        @NotNull(message = "썸네일 이미지는 필수입니다.")
        MultipartFile thumbnail
) {

    // 썸네일 크기 최대 5MB
    private static final long MAX_THUMBNAIL_SIZE_BYTES = 5 * 1024 * 1024;

    // 중복 검증 추가
    public CreateLectureCommand toCommand(Long teacherId, String thumbnailUrl) {
        LectureCategory lectureCategory = parseCategory(category);

        return new CreateLectureCommand(
                teacherId,
                title,
                description,
                thumbnailUrl,
                lectureCategory
        );
    }

    // S3 업로드 전에 카테고리를 먼저 검증
    public void validateCategory() {
        parseCategory(category);
    }

    private LectureCategory parseCategory(String category) {
        try {
            return LectureCategory.valueOf(category);
        } catch (IllegalArgumentException exception) {
            throw new DomainRuleViolationException("허용되지 않은 강의 카테고리입니다.");
        }
    }

    public void validateThumbnailSize() {
        if (thumbnail != null && thumbnail.getSize() > MAX_THUMBNAIL_SIZE_BYTES) {
            throw new DomainRuleViolationException("썸네일 파일 크기는 5MB 이하만 가능합니다.");
        }
    }
}