package com.wanted.momocity.lecture.presentation.api.response;

import com.wanted.momocity.lecture.domain.model.LectureChapter;

import java.time.LocalDateTime;

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