package com.wanted.momocity.viewing.application.usecase;

/*
* comment.
*  내 수강 강의 전체 목록 조회정
*  EnrollmentPort 로 수강 목록 조회 후 LecturePort 로 강의 정보 조회하여 반환
*  -
*  [반환 타입 변경 이유]
*  List<MyLectureResponse> -> MyLecturesResponse
*  -> 래핑 구조로 변경하여 확장성 확보
* */

import com.wanted.momocity.viewing.presentation.api.response.MyLecturesResponse;

public interface GetMyLectureUseCase {

    MyLecturesResponse getMyLectures (Long userId);

}
