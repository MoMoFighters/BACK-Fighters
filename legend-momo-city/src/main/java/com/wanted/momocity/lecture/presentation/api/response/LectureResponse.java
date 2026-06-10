package com.wanted.momocity.lecture.presentation.api.response;

import com.wanted.momocity.lecture.domain.model.LectureAggregate;
import com.wanted.momocity.lecture.domain.model.LectureChapter;

import java.time.LocalDateTime;
import java.util.List;

public class LectureResponse {

    private LectureResponse() {}

    // 강의 상태 변경 성공 응답 DTO.
    public record ChangeLectureStatusResponse(
            Long lectureId,
            String lectureStatus,
            LocalDateTime updatedAt
    ) {

        // LectureAggregate 도메인 모델을 응답 DTO로 변환
        public static ChangeLectureStatusResponse from(LectureAggregate lecture) {
            return new ChangeLectureStatusResponse(
                    lecture.getId(),
                    lecture.getStatus().name(),
                    lecture.getUpdatedAt()
            );
        }
    }

    // CreateChapterResponse는 챕터 등록 성공 시 프론트에 내려주는 응답 DTO
    public record CreateChapterResponse(
            Long chapterId,
            Long lectureId,
            String title,
            int orderNo,
            String videoUrl,
            Long videoSizeBytes,
            Integer durationSec,
            String videoStatus,
            String originalFilename,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {

        // 도메인 모델 LectureChapter를 응답 DTO로 변환
        public static CreateChapterResponse from(LectureChapter chapter) {
            return new CreateChapterResponse(
                    chapter.getId(),
                    chapter.getLectureId(),
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

    // CreateLectureResponse는 강의 등록 성공 시 프론트에 내려주는 응답 DTO
    public record CreateLectureResponse(
            Long lectureId,
            Long teacherId,
            String title,
            String description,
            String thumbnailUrl,
            String category,
            String lectureStatus,
            int completedUserCount,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {

        // 도메인 모델 Lecture를 응답 DTO로 변환
        public static CreateLectureResponse from(LectureAggregate lecture) {
            return new CreateLectureResponse(
                    lecture.getId(),
                    lecture.getTeacherId(),
                    lecture.getTitle(),
                    lecture.getDescription(),
                    lecture.getThumbnailUrl(),
                    lecture.getCategory().name(),
                    lecture.getStatus().name(),
                    lecture.getCompletedUserCount(),
                    lecture.getCreatedAt(),
                    lecture.getUpdatedAt()
            );
        }
    }

    // LectureListItemResponse는 강의 목록의 강의 1개를 표현하는 응답 DTO
    public record LectureListItemResponse(

            // 수강신청 ID
            Long enrollmentId,

            // 강의 ID
            Long lectureId,

            // 강의 제목
            String title,

            // 강의 썸네일 이미지 URL
            String thumbnailUrl,

            // 강의 카테고리
            String category,

            // 강의 상태
            String lectureStatus,

            // 로그인 사용자가 이 강의를 수강 신청했는지 여부
            boolean enrolled,

            // 전체 진도율
            int totalProgress,

            // 완료한 챕터 수
            int completedCount

    ) {
    }

    // LecturePageResponse는 강의 목록 페이지 응답 DTO
    public record LecturePageResponse(

            // 현재 페이지의 강의 목록
            List<LectureListItemResponse> content,

            // 현재 페이지 번호
            int page,

            // 한 페이지 크기
            int size,

            // 전체 강의 개수
            long totalElements,

            // 전체 페이지 수입니다.
            int totalPages

    ) {
    }

    // 챕터 동영상 등록 성공 응답 DTO
    public record RegisterChapterVideoResponse(
            Long chapterId,
            Long lectureId,
            String title,
            int orderNo,
            String videoUrl,
            Long videoSizeBytes,
            Integer durationSec,
            String videoStatus,
            String originalFilename,
            LocalDateTime updatedAt
    ) {

        // LectureChapter 도메인 모델을 응답 DTO로 변환
        public static RegisterChapterVideoResponse from(LectureChapter chapter) {
            return new RegisterChapterVideoResponse(
                    chapter.getId(),
                    chapter.getLectureId(),
                    chapter.getTitle(),
                    chapter.getOrderNo(),
                    chapter.getVideoUrl(),
                    chapter.getVideoSizeBytes(),
                    chapter.getDurationSec(),
                    chapter.getVideoStatus().name(),
                    chapter.getOriginalFilename(),
                    chapter.getUpdatedAt()
            );
        }
    }


}
