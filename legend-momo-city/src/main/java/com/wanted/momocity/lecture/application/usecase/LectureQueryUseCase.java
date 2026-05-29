package com.wanted.momocity.lecture.application.usecase;

import com.wanted.momocity.lecture.application.query.GetLecturesQuery;
import com.wanted.momocity.lecture.application.query.GetStudentLectureDetailQuery;
import com.wanted.momocity.lecture.application.query.GetTeacherLectureDetailQuery;
import com.wanted.momocity.lecture.application.query.GetTeacherLecturesQuery;
import com.wanted.momocity.lecture.presentation.api.response.StudentLectureDetailResponse;
import com.wanted.momocity.lecture.presentation.api.response.StudentLecturePageResponse;
import com.wanted.momocity.lecture.presentation.api.response.TeacherLectureDetailResponse;
import com.wanted.momocity.lecture.presentation.api.response.TeacherLecturePageResponse;

// 조회 관련 UseCase
public interface LectureQueryUseCase {

    // 학생용 강의 목록 조회
    StudentLecturePageResponse getLectures(GetLecturesQuery query);

    // 강사용 강의 목록 조회
    TeacherLecturePageResponse getTeacherLectures(GetTeacherLecturesQuery query);

    // 강사용 강의 상세 조회
    TeacherLectureDetailResponse getTeacherLectureDetail(GetTeacherLectureDetailQuery query);

    // 학생 강의 상세 조회
    StudentLectureDetailResponse getStudentLectureDetail(GetStudentLectureDetailQuery query);
}