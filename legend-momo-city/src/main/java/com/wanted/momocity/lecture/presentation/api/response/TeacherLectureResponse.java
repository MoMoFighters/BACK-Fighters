package com.wanted.momocity.lecture.presentation.api.response;

import com.wanted.momocity.lecture.domain.model.LectureAggregate;
import com.wanted.momocity.lecture.domain.model.LectureChapter;

import java.time.LocalDateTime;
import java.util.List;

public class TeacherLectureResponse {

    private TeacherLectureResponse() {}

    // 강사 강의 상세 조회에서 챕터 1개의 정보를 내려주는 응답 DTO
    public record TeacherLectureChapterResponse(

            // 챕터 ID
            Long chapterId,

            // 챕터 제목
            String title,

            // 강의 안에서 챕터가 보여질 순서
            int orderNo,

            // 등록된 동영상 URL
            String videoUrl,

            // 등록된 동영상 파일 크기
            Long videoSizeBytes,

            // 동영상 재생 시간입니다. 단위는 초
            Integer durationSec,

            // 동영상 처리 상태
            String videoStatus,

            // 업로드한 원본 파일명
            String originalFilename,

            // 챕터 생성 일시
            LocalDateTime createdAt,

            // 챕터 수정 일시
            LocalDateTime updatedAt

    ) {

        // 도메인 모델 LectureChapter를 응답 DTO로 변환
        public static TeacherLectureChapterResponse from(LectureChapter chapter) {
            return new TeacherLectureChapterResponse(
                    chapter.getId(),
                    chapter.getTitle(),
                    chapter.getOrderNo(),
                    chapter.getVideoUrl(),
                    chapter.getVideoSizeBytes(),
                    chapter.getDurationSec(),
                    chapter.getVideoStatus().name(),
                    chapter.getOriginalFilename(),
                    chapter.getCreatedAt(),
                    chapter.getUpdatedAt()
            );
        }
    }

    // 강사 강의 상세 조회에서 강의 1개의 상세 정보를 내려주는 응답 DTO
    public record TeacherLectureDetailResponse(

            // 강의 ID
            Long lectureId,

            // 강의 제목
            String title,

            // 강의 설명
            String description,

            // 강의 썸네일 이미지 URL
            String thumbnailUrl,

            // 강의 카테고리
            String category,

            // 강의 상태
            String lectureStatus,

            // 강의 평점 평균
            double averageRating,

            // 리뷰 개수
            int reviewCount,

            // 강의에 포함된 챕터 목
            List<TeacherLectureChapterResponse> chapters,

            // 강의 생성 일시
            LocalDateTime createdAt,

            // 강의 수정 일시
            LocalDateTime updatedAt

    ) {

        // LectureAggregate와 챕터 목록을 강의 상세 응답 DTO로 변환
        public static TeacherLectureDetailResponse from(
                LectureAggregate lecture,
                List<LectureChapter> chapters,
                double averageRating,
                int reviewCount
        ) {
            return new TeacherLectureDetailResponse(
                    lecture.getId(),
                    lecture.getTitle(),
                    lecture.getDescription(),
                    lecture.getThumbnailUrl(),
                    lecture.getCategory().name(),
                    lecture.getStatus().name(),
                    averageRating,
                    reviewCount,                chapters.stream()
                    .map(TeacherLectureChapterResponse::from)
                    .toList(),
                    lecture.getCreatedAt(),
                    lecture.getUpdatedAt()
            );
        }
    }

    // 강사 강의 목록에서 강의 1개를 표현하는 응답 DTO

    /* comment
     *   여기서 isEnabled는 넣지 않는다.
     *   강사 목록 조회는 본인 강의를 출력을 하는데 수강여부가 아닌 관리 대상 여부가 중요하다.
     * */
    public record TeacherLectureListItemResponse(
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
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {

        // 도메인 모델 LectureAggregate를 강사용 목록 응답 DTO
        public static TeacherLectureListItemResponse from(
                LectureAggregate lecture,
                double averageRating,
                int reviewCount
        ) {
            return new TeacherLectureListItemResponse(
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

    // 강사 강의 목록 페이지 응답 DTO
    public record TeacherLecturePageResponse(
            // 강의 목록 여러개를 담기 위해 List 사용
            List<TeacherLectureListItemResponse> content,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {
    }

}
