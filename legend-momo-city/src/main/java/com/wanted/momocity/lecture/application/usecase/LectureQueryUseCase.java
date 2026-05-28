package com.wanted.momocity.lecture.application.usecase;

import com.wanted.momocity.lecture.application.query.GetLecturesQuery;
import com.wanted.momocity.lecture.application.query.GetTeacherLecturesQuery;
import com.wanted.momocity.lecture.presentation.api.response.LecturePageResponse;
import com.wanted.momocity.lecture.presentation.api.response.TeacherLecturePageResponse;

/**
 * LectureQueryUseCase는 강의 조회 기능
 *
 * Controller는 이 인터페이스를 통해
 * 강의 조회 기능을 호출
 */
public interface LectureQueryUseCase {

    /*
     * 강의 목록을 조회
     * GetLecturesQuery 안에는
     * category, enrolled, page, size 같은 조회 조건이 들어 있음
     */
    LecturePageResponse getLectures(GetLecturesQuery query);

    // 강사용 강의 목록 조회
    // 로그인한 강사가 본인이 등록한 강의만 조회
    TeacherLecturePageResponse getTeacherLectures(GetTeacherLecturesQuery query);
}