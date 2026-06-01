package com.wanted.momocity.viewing.presentation.api.response;

import java.time.LocalDateTime;
import java.util.List;

/*
* comment.
*  내 수강 강의 목록 반환 (래핑)
*  MyLecturesResponse.LectureItem 으로 내부 아이템 모델 접근
*  → MyLecturesResponse 에서만 쓰는 아이템 모델이라 안에 묶음
*  (ChapterProgressResponse.ChapterProgressItem 과 동일한 패턴)
* */

public record MyLecturesResponse(
        List<LectureItem> lectures
) {
    // 수강 강의 단건 정보
    // enrolledAt 은 EnrollmentJpaRepository 완성 후 추가 예정
    public record LectureItem(
            Long lectureId,
            String lectureTitle,
            String thumbnailUrl,
            String category,
            int totalProgress,
            LocalDateTime enrolledAt
    ) {}
}
