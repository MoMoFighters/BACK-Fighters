package com.wanted.momocity.lecture.presentation.api.response;

import com.wanted.momocity.lecture.domain.model.LectureAggregate;
import com.wanted.momocity.lecture.domain.model.LectureChapter;
import com.wanted.momocity.lecture.domain.model.VideoStatus;

import java.time.LocalDateTime;
import java.util.List;

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