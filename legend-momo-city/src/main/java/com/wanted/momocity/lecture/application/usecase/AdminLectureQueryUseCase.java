package com.wanted.momocity.lecture.application.usecase;

import com.wanted.momocity.lecture.application.query.GetAdminLectureDetailQuery;
import com.wanted.momocity.lecture.application.query.GetAdminLecturesQuery;
import com.wanted.momocity.lecture.presentation.api.response.AdminLectureDetailResponse;
import com.wanted.momocity.lecture.presentation.api.response.AdminLecturePageResponse;

// 관리자 강의 조회 기능을 정의하는 UseCase 인터페이스
public interface AdminLectureQueryUseCase {

    // 관리자가 강의 목록을 조회
    AdminLecturePageResponse getAdminLectures(GetAdminLecturesQuery query);

    // 관리자가 강의 상세 정보를 조회한다.
    AdminLectureDetailResponse getAdminLectureDetail(GetAdminLectureDetailQuery query);
}