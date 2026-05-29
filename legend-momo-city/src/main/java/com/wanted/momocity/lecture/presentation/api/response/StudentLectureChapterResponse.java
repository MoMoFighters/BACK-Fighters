package com.wanted.momocity.lecture.presentation.api.response;

import com.wanted.momocity.lecture.domain.model.LectureChapter;

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