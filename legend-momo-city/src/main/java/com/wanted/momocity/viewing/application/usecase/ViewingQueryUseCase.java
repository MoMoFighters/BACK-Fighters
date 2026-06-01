package com.wanted.momocity.viewing.application.usecase;

import com.wanted.momocity.viewing.presentation.api.response.*;

/*
 * comment.
 *  Viewing 컨텍스트의 읽기 작업 전용 UseCase
 *  → 상태를 변경하지 않는 조회 작업만 담당
 *  → @Transactional(readOnly = true) 적용
 *  -
 *  [담당 조회]
 *  - getStreamingUrl    : S3 Presigned URL 발급
 *  - getLectureMeta     : 강의 메타데이터 조회
 *  - getChapterResume   : 챕터 이어보기 지점 조회
 *  - getTotalProgress   : 전체 진척도 조회
 *  - getChapterProgress : 챕터별 진척도 조회
 *  - getMyLectures      : 내 수강 강의 목록 조회
 */

public interface ViewingQueryUseCase {

    StreamingUrlResponse getStreamingUrl(Long userId, Long lectureId, Long chapterId);

    LectureMetaResponse getLectureMeta(Long userId, Long lectureId);

    ChapterResumeResponse getChapterResume(Long userId, Long lectureId, Long chapterId);

    TotalProgressResponse getTotalProgress(Long userId, Long lectureId);

    ChapterProgressResponse getChapterProgress(Long userId, Long lectureId);

    MyLecturesResponse getMyLectures(Long userId);
}
