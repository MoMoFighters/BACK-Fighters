package com.wanted.momocity.lecture.presentation.api.response;

import com.wanted.momocity.lecture.domain.model.LectureChapter;

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