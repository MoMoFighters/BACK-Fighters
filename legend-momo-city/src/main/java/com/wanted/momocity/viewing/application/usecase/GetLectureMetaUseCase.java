package com.wanted.momocity.viewing.application.usecase;

import com.wanted.momocity.viewing.presentation.api.response.LectureMetaResponse;

/*
* comment.
*  강의 메타데이터 조회 (플레이어 UI 상단용)
* */

public interface GetLectureMetaUseCase {

    LectureMetaResponse getLectureMeta (Long userId, Long lectureId);

}
