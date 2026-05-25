package com.wanted.momocity.viewing.application.usecase;

import com.wanted.momocity.viewing.presentation.api.response.MyLectureResponse;

import java.util.List;

/*
* comment.
*  내 수강 강의 전체 목록 조회
* */
public interface GetMyLectureUseCase {

    List<MyLectureResponse> getMyLectures (Long userId);

}
