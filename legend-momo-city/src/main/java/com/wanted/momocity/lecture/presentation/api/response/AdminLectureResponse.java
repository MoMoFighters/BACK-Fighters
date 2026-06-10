package com.wanted.momocity.lecture.presentation.api.response;

import com.wanted.momocity.lecture.domain.model.LectureAggregate;
import com.wanted.momocity.lecture.domain.model.LectureChapter;

import java.time.LocalDateTime;
import java.util.List;

public class AdminLectureResponse {

    private AdminLectureResponse() {}

    // 관리자 강의 상태 변경 성공 응답 DTO
    public record AdminChangeLectureStatusResponse(
            Long lectureId,             // 상태가 변경된 강의 ID
            String lectureStatus,       // 변경된 강의 상태
            LocalDateTime updatedAt     // 상태 변경 시각
    ) {

        // 도메인 모델에서 필요한 값만 응답 DTO로 변환
        public static AdminChangeLectureStatusResponse from(LectureAggregate lecture) {
            return new AdminChangeLectureStatusResponse(
                    lecture.getId(),
                    lecture.getStatus().name(),
                    lecture.getUpdatedAt()
            );
        }
    }

    // 관리자 강의 상세 조회에서 챕터 1개의 정보를 내려주는 응답 DTO
    /*  comment
     *   영상 정보까지 넣은 이유는 관리자는 챕터, 영상, 영상 상태, 원본 파일명을 봐야 되기 때문에
     *   학생 응답보다 더 많은 정보를 넣음
     * */
    public record AdminLectureChapterResponse(
            Long chapterId,
            Long lectureId,
            String title,
            int orderNo,
            String videoUrl,
            Long videoSizeBytes,
            Integer durationSec,
            String videoStatus,
            String originalFilename
    ) {

        // 도메인 모델 LectureChapter를 관리자 챕터 응답 DTO로 변환
        public static AdminLectureChapterResponse from(LectureChapter chapter) {
            return new AdminLectureChapterResponse(
                    chapter.getId(),
                    chapter.getLectureId(),
                    chapter.getTitle(),
                    chapter.getOrderNo(),
                    chapter.getVideoUrl(),
                    chapter.getVideoSizeBytes(),
                    chapter.getDurationSec(),
                    chapter.getVideoStatus().name(),
                    chapter.getOriginalFilename()
            );
        }
    }

    // 관리자 강의 상세 조회 응답 DTO
    public record AdminLectureDetailResponse(
            Long lectureId,
            Long teacherId,
            String title,
            String description,
            String thumbnailUrl,
            String category,
            String lectureStatus,
            int completedUserCount,
            double averageRating,
            int reviewCount,
            List<AdminLectureChapterResponse> chapters,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {

        // 강의 도메인 모델과 챕터 목록을 관리자 상세 응답 DTO로 변환한다.
        public static AdminLectureDetailResponse from(
                LectureAggregate lecture,
                List<LectureChapter> chapters,
                double averageRating,
                int reviewCount
        ) {
            return new AdminLectureDetailResponse(
                    lecture.getId(),
                    lecture.getTeacherId(),
                    lecture.getTitle(),
                    lecture.getDescription(),
                    lecture.getThumbnailUrl(),
                    lecture.getCategory().name(),
                    lecture.getStatus().name(),
                    lecture.getCompletedUserCount(),
                    averageRating,
                    reviewCount,
                    chapters.stream()
                            .map(AdminLectureChapterResponse::from)
                            .toList(),
                    lecture.getCreatedAt(),
                    lecture.getUpdatedAt()
            );
        }
    }

    // 관리자 강의 목록에서 강의 1개를 표현하는 응답 DTO
    public record AdminLectureListItemResponse(
            Long lectureId,              // 강의 ID
            Long teacherId,              // 강사 ID
            String title,                // 강의 제목
            String description,          // 강의 설명
            String thumbnailUrl,         // 썸네일 URL
            String category,             // 강의 카테고리
            String lectureStatus,        // 강의 상태
            int completedUserCount,      // 수강 완료 인원
            double averageRating,        // 평균 평점
            int reviewCount,             // 리뷰 개수
            LocalDateTime createdAt,     // 강의 생성일
            LocalDateTime updatedAt      // 강의 수정일
    ) {

        /* comment
         * 도메인 모델 LectureAggregate를 관리자 목록 응답 DTO로 변환한다.
         * averageRating, reviewCount는 추후 review 패키지와 port 연결 후 실제 값으로 교체한다.
         */
        public static AdminLectureListItemResponse from(
                LectureAggregate lecture,
                double averageRating,
                int reviewCount
        ) {
            return new AdminLectureListItemResponse(
                    lecture.getId(),
                    lecture.getTeacherId(),
                    lecture.getTitle(),
                    lecture.getDescription(),
                    lecture.getThumbnailUrl(),
                    lecture.getCategory().name(),
                    lecture.getStatus().name(),
                    lecture.getCompletedUserCount(),
                    averageRating,
                    reviewCount,
                    lecture.getCreatedAt(),
                    lecture.getUpdatedAt()
            );
        }
    }

    // 관리자 강의 목록 페이지 응답 DTO
    public record AdminLecturePageResponse(
            List<AdminLectureListItemResponse> content,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {
    }
}
