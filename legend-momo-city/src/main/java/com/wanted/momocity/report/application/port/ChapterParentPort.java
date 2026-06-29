package com.wanted.momocity.report.application.port;

/* comment.
    CHAPTER 타입 신고의 부모 강의 ID 조회 : lecture BC 의 ChapterParentAdapter 가 구현
 */

public interface ChapterParentPort {

    Long getLectureIdByChapterId(Long chapterId);
}
