package com.wanted.momocity.lecture.presentation.api.request;

import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.momocity.lecture.application.command.LectureCommand;
import com.wanted.momocity.lecture.domain.model.LectureCategory;
import com.wanted.momocity.lecture.domain.model.LectureStatus;
import com.wanted.momocity.lecture.domain.model.VideoStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public final class LectureRequest {

    private LectureRequest() {
    }
    // 관리자 강의 상태 변경 요청 DTO
    public record AdminChangeLectureStatusRequest(
            String lectureStatus // ACTIVE 또는 HOLD
    ) {

        /* comment
         * Controller에서 adminId, lectureId를 함께 받아 Command로 변환
         * adminId는 Authorization 토큰에서 가져온 사용자 ID
         * lectureId는 URL PathVariable애서 가져온다.
         */
        public LectureCommand.AdminChangeLectureStatusCommand toCommand(
                Long adminId,
                Long lectureId
        ) {
            return new LectureCommand.AdminChangeLectureStatusCommand(
                    adminId,
                    lectureId,
                    parseLectureStatus(lectureStatus)
            );
        }

        // 문자열로 들어온 lectureStatus를 enum으로 변환
        private LectureStatus parseLectureStatus(String lectureStatus) {
            if (lectureStatus == null || lectureStatus.isBlank()) {
                throw new DomainRuleViolationException("강의 상태는 필수입니다.");
            }

            try {
                return LectureStatus.valueOf(lectureStatus.toUpperCase());
            } catch (IllegalArgumentException exception) {
                throw new DomainRuleViolationException("허용되지 않은 강의 상태입니다.");
            }
        }
    }


    // 챕터 동영상 상태 변경 요청 DTO
    public record ChangeChapterVideoStatusRequest(
            String videoStatus
    ) {
        public LectureCommand.ChangeChapterVideoStatusCommand toCommand(
                Long teacherId,
                Long lectureId,
                Long chapterId
        ) {
            return new LectureCommand.ChangeChapterVideoStatusCommand(
                    teacherId,
                    lectureId,
                    chapterId,
                    parseVideoStatus(videoStatus)
            );
        }

        private VideoStatus parseVideoStatus(String videoStatus) {
            if (videoStatus == null || videoStatus.isBlank()) {
                throw new DomainRuleViolationException("동영상 상태는 필수입니다.");
            }

            try {
                return VideoStatus.valueOf(videoStatus.toUpperCase());
            } catch (IllegalArgumentException exception) {
                throw new DomainRuleViolationException("허용되지 않은 동영상 상태입니다.");
            }
        }
    }

    // 강의 상태 변경 요청 DTO
    public record ChangeLectureStatusRequest(

            // 변경할 강의 상태입니다. 예: ACTIVE, HOLD, DELETED, WAITING
            @NotBlank(message = "강의 상태는 필수입니다.")
            String lectureStatus
    ) {

        // 문자열로 받은 상태값을 LectureStatus enum으로 변환한 뒤 Command로 만든다.
        public LectureCommand.ChangeLectureStatusCommand toCommand(
                Long teacherId,
                Long lectureId
        ) {
            return new LectureCommand.ChangeLectureStatusCommand(
                    teacherId,
                    lectureId,
                    parseLectureStatus()
            );
        }

        /* comment
         * 요청 상태값을 LectureStatus enum으로 변환
         * 허용되지 않은 값이면 400으로 처리될 도메인 예외를 던집니다.
         */
        private LectureStatus parseLectureStatus() {
            try {
                return LectureStatus.valueOf(lectureStatus.toUpperCase());
            } catch (IllegalArgumentException exception) {
                throw new DomainRuleViolationException("허용되지 않은 강의 상태입니다.");
            }
        }
    }

    // CreateChapterRequest는 챕터 등록 JSON 요청을 받는 DTO
    public record CreateChapterRequest(
            // 챕터명은 필수
            @NotBlank(message = "챕터명은 필수입니다.")
            String title,

            // 챕터 순서는 1 이상
            @Min(value = 1, message = "챕터 순서는 1 이상이어야 합니다.")
            int orderNo
    ) {

        // Controller에서 받은 요청값을 application 계층의 Command로 변환
        public LectureCommand.CreateChapterCommand toCommand(Long teacherId, Long lectureId) {
            return new LectureCommand.CreateChapterCommand(
                    teacherId,
                    lectureId,
                    title,
                    orderNo
            );
        }
    }

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
        public LectureCommand.CreateLectureCommand toCommand(Long teacherId, String thumbnailUrl) {
            LectureCategory lectureCategory = parseCategory(category);

            return new LectureCommand.CreateLectureCommand(
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

    // 챕터 동영상 등록 요청 DTO
    public record RegisterChapterVideoRequest(

            // 업로드할 동영상 파일
            @NotNull(message = "동영상 파일은 필수입니다.")
            MultipartFile video,

            // 동영상 재생 시간
            @NotNull(message = "동영상 재생 시간은 필수입니다.")
            @Min(value = 1, message = "동영상 재생 시간은 1초 이상이어야 합니다.")
            Integer durationSec
    ) {

        /* comment
         * Presentation 계층의 요청 DTO를 Application 계층의 Command로 변환
         * Controller는 HTTP 요청만 알고, Service는 Command만 보고 처리하게 분리
         */
        public LectureCommand.RegisterChapterVideoCommand toCommand(
                Long teacherId,
                Long lectureId,
                Long chapterId
        ) {
            return new LectureCommand.RegisterChapterVideoCommand(
                    teacherId,
                    lectureId,
                    chapterId,
                    video,
                    durationSec
            );
        }
    }

}