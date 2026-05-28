package com.wanted.momocity.lecture.presentation.api.response;

import com.wanted.momocity.lecture.domain.model.LectureChapter;

import java.time.LocalDateTime;

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