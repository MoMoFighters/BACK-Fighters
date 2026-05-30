package com.wanted.momocity.lecture.presentation.api.response;

import com.wanted.momocity.lecture.domain.model.LectureAggregate;
import com.wanted.momocity.lecture.domain.model.LectureChapter;

import java.time.LocalDateTime;
import java.util.List;

// 강사 강의 상세 조회에서 강의 1개의 상세 정보를 내려주는 응답 DTO
public record TeacherLectureDetailResponse(

        // 강의 ID
        Long lectureId,

        // 강사 ID
        Long teacherId,

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

        // 수강 완료 사용자 수
        int completedUserCount,

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
                lecture.getTeacherId(),
                lecture.getTitle(),
                lecture.getDescription(),
                lecture.getThumbnailUrl(),
                lecture.getCategory().name(),
                lecture.getStatus().name(),
                lecture.getCompletedUserCount(),
                averageRating,
                reviewCount,                chapters.stream()
                        .map(TeacherLectureChapterResponse::from)
                        .toList(),
                lecture.getCreatedAt(),
                lecture.getUpdatedAt()
        );
    }
}