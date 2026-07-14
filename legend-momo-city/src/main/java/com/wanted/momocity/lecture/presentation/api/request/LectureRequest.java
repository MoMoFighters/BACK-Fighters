package com.wanted.momocity.lecture.presentation.api.request;

import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.momocity.lecture.application.command.LectureCommand;
import com.wanted.momocity.lecture.domain.model.LectureCategory;
import com.wanted.momocity.lecture.domain.model.LectureStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.checkerframework.checker.units.qual.K;
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
            int orderNo,

            @NotNull(message = "챕터 썸네일 이미지는 필수입니다.")
            MultipartFile thumbnail
    ) {
        // 썸네일 크기 제한
        private static final long MAX_CHAPTER_THUMBNAIL_SIZE_BYTES = 5*1024*1024;

        // Controller에서 받은 요청값을 application 계층의 Command로 변환
        public LectureCommand.CreateChapterCommand toCommand(Long teacherId, Long lectureId) {
            return new LectureCommand.CreateChapterCommand(
                    teacherId,
                    lectureId,
                    title,
                    orderNo,
                    thumbnail
            );
        }

        public void validateThumbnailSize() {
            if (thumbnail != null && thumbnail.getSize() > MAX_CHAPTER_THUMBNAIL_SIZE_BYTES) { // 파일이 있고 5MB를 초과하는지 확인
                throw new DomainRuleViolationException("챕터 썸네일 파일 크기는 5MB 이하만 가능합니다."); // 5MB 초과 시 도메인 예외 발생
            }
        }
    }

    // 챕터 텍스트 수정
    public record UpdateChapterRequest(
            @NotBlank(message = "챕터 제목은 필수입니다.")
            String title,

            // 챕터는 1개 이상
            @Min(value = 1, message = "챕터 순서는 1 이상이어야 합니다.")
            int orderNo
    ) {
        public LectureCommand.UpdateChapterCommand toCommand(Long teacherId, Long lectureId, Long chapterId){
            return new LectureCommand.UpdateChapterCommand(
                    teacherId,
                    lectureId,
                    chapterId,
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
        public LectureCommand.CreateLectureCommand toCommand(Long teacherId) {
            LectureCategory lectureCategory = parseCategory(category);

            return new LectureCommand.CreateLectureCommand(
                    teacherId,
                    title,
                    description,
                    thumbnail,
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

    // 강의 텍스트 수정 요청 DTO
    public record UpdateLectureRequest(

            // 강의 제목, 내용, 카테고리는 필수이기 때문에 비어 있으면 @NotBlank로 막기
            @NotBlank(message = "강의 제목은 필수입니다.")
            String title,

            @NotBlank( message = "강의 설명은 필수입니다.")
            String description,

            @NotBlank(message = "강의 카테고리는 필수입니다.")
            String category
    ) {
        public LectureCommand.UpdateLectureCommand toCommand(Long teacherId, Long lectureId) {

            // 카테고리 문자열을 ENUM 값으로 변경
            LectureCategory lectureCategory = parseCategory(category);

            return new LectureCommand.UpdateLectureCommand(
                    teacherId,
                    lectureId,
                    title,
                    description,
                    lectureCategory
            );
        }

        private LectureCategory parseCategory(@NotBlank(message = "강의 카테고리는 필수입니다.") String category) {
            try {
                return LectureCategory.valueOf(category);
            } catch (IllegalArgumentException exception) {
                throw new DomainRuleViolationException("허용되지 않은 강의 카테고리입니다.");
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