package com.wanted.momocity.viewing.application.usecase;

import com.wanted.momocity.viewing.presentation.api.response.ChapterProgressResponse;

/*
* comment.
*  챕터별 진척도 조회
* */

public interface GetChapterProgressUseCase {

    ChapterProgressResponse getChapterProgress (Long userId, Long lectureId);

}
