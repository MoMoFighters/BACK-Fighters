package com.wanted.momocity.viewing.application.usecase;

import com.wanted.momocity.viewing.presentation.api.response.ChapterResumeResponse;

/*
* comment.
*  챕터 이어보기 시작 지점 조회
* */

public interface GetChapterResumeUseCase {

    ChapterResumeResponse getChapterResume (Long userId, Long lectureId, Long chapterId);

}
