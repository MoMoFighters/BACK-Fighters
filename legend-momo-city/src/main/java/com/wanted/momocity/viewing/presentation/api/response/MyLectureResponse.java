package com.wanted.momocity.viewing.presentation.api.response;

/*
* comment.
*  내 수강 강의 목록 단건 반환
* */

public record MyLectureResponse(
        Long lectureId,
        String lectureTitle,
        String thumbnailUrl,
        String category,
        int totalProgress
) {
}
