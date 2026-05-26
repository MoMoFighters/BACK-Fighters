package com.wanted.momocity.lecture.presentation.api.request;

import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.momocity.lecture.application.command.CreateLectureCommand;
import com.wanted.momocity.lecture.application.command.LectureThumbnailFile;
import com.wanted.momocity.lecture.domain.model.LectureCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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

    public CreateLectureCommand toCommand(String teacherEmail, String thumbnailUrl) {
        return new CreateLectureCommand(
                teacherEmail,
                title,
                description,
                thumbnailUrl,
                parseCategory(category)
        );
    }


    // form-data로 받은 MultipartFile을 application 계층에서 사용할 파일 객체로 변환한다.

    public LectureThumbnailFile toThumbnailFile() {
        try {
            return new LectureThumbnailFile(
                    thumbnail.getOriginalFilename(),
                    thumbnail.getContentType(),
                    thumbnail.getSize(),
                    thumbnail.getBytes()
            );
        } catch (IOException exception) {
            throw new DomainRuleViolationException("썸네일 파일을 읽을 수 없습니다.");
        }
    }

    private LectureCategory parseCategory(String category) {
        try {
            return LectureCategory.valueOf(category);
        } catch (IllegalArgumentException exception) {
            throw new DomainRuleViolationException("허용되지 않은 강의 카테고리입니다.");
        }
    }

    /*
     * category 값을 먼저 검증
     * S3 업로드 전에 호출해서 잘못된 카테고리 요청일 때 파일이 업로드 X
     */
    public void validateCategory() {
        parseCategory(category);
    }
}