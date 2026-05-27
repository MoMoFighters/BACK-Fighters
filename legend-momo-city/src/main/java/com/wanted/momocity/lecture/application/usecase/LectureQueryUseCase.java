package com.wanted.momocity.lecture.application.usecase;

import com.wanted.momocity.lecture.application.query.GetLecturesQuery;
import com.wanted.momocity.lecture.presentation.api.response.LecturePageResponse;

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
}