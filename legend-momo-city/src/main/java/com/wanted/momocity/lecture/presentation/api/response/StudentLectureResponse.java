package com.wanted.momocity.lecture.presentation.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.wanted.momocity.lecture.domain.model.LectureAggregate;
import com.wanted.momocity.lecture.domain.model.LectureChapter;
import com.wanted.momocity.lecture.domain.model.VideoStatus;

import java.time.LocalDateTime;
import java.util.List;

public class StudentLectureResponse {

    private StudentLectureResponse() {}

    // 학생 강의 상세 조회에서 챕터 1개의 정보를 내려주는 응답 DTO
    public record StudentLectureChapterResponse(

            // 챕터 ID
            Long chapterId,

            // 챕터 제목
            String title,

            // 강의 안에서 챕터가 보여질 순서
            int orderNo,

            // 동영상 재생 시간입니다. 단위는 초
            Integer durationSec,

            // 동영상 처리 상태. 예: UPLOADING, ENCODING, READY, FAILED
            String videoStatus

    ) {

        // LectureChapter 도메인 객체를 학생용 챕터 응답 DTO로 변환
        public static StudentLectureChapterResponse from(LectureChapter chapter) {
            return new StudentLectureChapterResponse(
                    chapter.getId(),
                    chapter.getTitle(),
                    chapter.getOrderNo(),
                    chapter.getDurationSec(),
                    chapter.getVideoStatus().name()
            );
        }
    }

    // 학생 강의 상세 조회 응답 DTO
    public record StudentLectureDetailResponse(

            // 강의 ID
            Long lectureId,

            // 강사 ID
            Long teacherId,

            // 강사 이름
            String teacherName,

            // 강사 프로필 이미지 URL
            String teacherProfileImageUrl,

            // 강의 제목
            String title,

            // 강의 설명
            String description,

            // 강의 썸네일 이미지 URL
            String thumbnailUrl,

            // 강의 카테고리
            String category,

            // 강의 상태입니다. 학생 상세 조회에서는 ACTIVE만 조회 가능
            String lectureStatus,

            // 강의를 완료한 사용자 수
            int completedUserCount,

            // 리뷰 평균 평점
            double averageRating,

            // 리뷰 개수
            int reviewCount,

            // 로그인한 학생이 이 강의를 수강신청했는지 여부
            boolean isEnrolled,

            // 강의에 포함된 챕터 목록
            List<StudentLectureChapterResponse> chapters,

            // 강의 생성 시간
            LocalDateTime createdAt,

            // 강의 수정 시간
            LocalDateTime updatedAt

    ) {

        // 강의, 챕터 목록, 부가 정보를 학생 상세 응답 DTO로 변환
        public static StudentLectureDetailResponse from(
                LectureAggregate lecture,
                List<LectureChapter> chapters,
                String teacherName,
                String teacherProfileImageUrl,
                double averageRating,
                int reviewCount,
                boolean isEnrolled
        ) {
            return new StudentLectureDetailResponse(
                    lecture.getId(),
                    lecture.getTeacherId(),
                    teacherName,
                    teacherProfileImageUrl,
                    lecture.getTitle(),
                    lecture.getDescription(),
                    lecture.getThumbnailUrl(),
                    lecture.getCategory().name(),
                    lecture.getStatus().name(),
                    lecture.getCompletedUserCount(),
                    averageRating,
                    reviewCount,
                    isEnrolled,
                    chapters.stream()
                            .filter(chapter -> chapter.getVideoStatus() == VideoStatus.READY)
                            .map(StudentLectureChapterResponse::from)
                            .toList(),
                    lecture.getCreatedAt(),
                    lecture.getUpdatedAt()
            );
        }
    }

    /* comment
     * 학생 강의 목록에서 강의 1개를 표현하는 응답 DTO
     * 최종 응답의 data.content 배열 안에 들어간다.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL) // <- Json 응답을 만들 때 null 값은 뺴겠다는 어노테이션
    public record StudentLectureListItemResponse(
            Long lectureId,              // 강의 ID
            String title,                // 강의 제목
            String description,          // 강의 설명
            String thumbnailUrl,         // 썸네일 URL
            String category,             // 강의 카테고리
            String lectureStatus,        // 강의 상태
            double averageRating,        // 평균 평점
            int reviewCount,             // 수강평 개수
            /* comment
            *   로그인한 학생이 해당 강의 수강 중이면 enrollment 진행률을 내려준다.(강의 총 진척도)
            *   비로그인 또는 미수강 강의면 0으로 내려준다
            * */
            Integer totalProgress,

            /* comment
            *   로그인 한 학생이 해당 강의를 완료했는지 여부 확인
            *   비로그인 또는 미수강이면 false 값
            * */
            Boolean isCompleted,

            int chapterCount,            // 해당 강의 전체 챕터 수
            LocalDateTime createdAt      // 강의 생성일
    ) {
        /* comment
         * 도메인 모델을 화면 응답 DTO로 변환
         * 목록 조회에서는 이 객체 여러 개가 content 배열에 담긴다.
         */
        public static StudentLectureListItemResponse from(
                LectureAggregate lecture,
                double averageRating,
                int reviewCount,
                Integer totalProgress,
                Boolean isCompleted,
                int chapterCount
        ) {
            return new StudentLectureListItemResponse(
                    lecture.getId(),
                    lecture.getTitle(),
                    lecture.getDescription(),
                    lecture.getThumbnailUrl(),
                    lecture.getCategory().name(),
                    lecture.getStatus().name(),
                    averageRating,
                    reviewCount,
                    totalProgress,
                    isCompleted,
                    chapterCount,
                    lecture.getCreatedAt()
            );
        }
    }

    /* comment
     * 학생 강의 목록 조회의 data 영역을 담당하는 응답 DTO
     * content 배열과 페이지 정보를 함께 내려준다.
     */
    public record StudentLecturePageResponse(
            List<StudentLectureListItemResponse> content,// 강의 목록 배열
            int page,                                     // 현재 페이지 번호
            int size,                                     // 한 페이지에 보여줄 강의 개수
            long totalElements,                          // 전체 강의 개수
            int totalPages                               // 전체 페이지 수
    ) {
    }
}
