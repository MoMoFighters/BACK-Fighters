package com.wanted.momocity.lecture.presentation.api.response;

import com.wanted.momocity.lecture.domain.model.LectureChapter;

import java.time.LocalDateTime;

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