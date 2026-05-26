package com.wanted.momocity.viewing.application.usecase;

import com.wanted.momocity.viewing.presentation.api.response.TotalProgressResponse;

/*
* comment.
*  강의 전체 진척도 조회
* */

public interface GetTotalProgressUseCase {

    TotalProgressResponse getTotalProgress (Long userId, Long lectureId);

}
