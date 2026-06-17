package com.wanted.momocity.lecture.application.usecase;

import com.wanted.momocity.lecture.application.query.LectureQuery.GetAdminLectureDetailQuery;
import com.wanted.momocity.lecture.application.query.LectureQuery.GetAdminLecturesQuery;
import com.wanted.momocity.lecture.application.query.LectureQuery.GetLecturesQuery;
import com.wanted.momocity.lecture.application.query.LectureQuery.GetTeacherLecturesQuery;
import com.wanted.momocity.lecture.application.query.LectureQuery.GetTeacherLectureDetailQuery;
import com.wanted.momocity.lecture.application.query.LectureQuery.GetStudentLectureDetailQuery;
import com.wanted.momocity.lecture.presentation.api.response.AdminLectureResponse.AdminLectureDetailResponse;
import com.wanted.momocity.lecture.presentation.api.response.AdminLectureResponse.AdminLecturePageResponse;
import com.wanted.momocity.lecture.presentation.api.response.StudentLectureResponse.StudentLecturePageResponse;
import com.wanted.momocity.lecture.presentation.api.response.StudentLectureResponse.StudentLectureDetailResponse;
import com.wanted.momocity.lecture.presentation.api.response.TeacherLectureResponse.TeacherLecturePageResponse;
import com.wanted.momocity.lecture.presentation.api.response.TeacherLectureResponse.TeacherLectureDetailResponse;

// 조회 관련 UseCase
public final class LectureQueryUseCases {

    private LectureQueryUseCases() {}

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

    // 관리자 강의 조회 기능을 정의하는 UseCase 인터페이스
    public interface AdminLectureQueryUseCase {

        // 관리자가 강의 목록을 조회
        AdminLecturePageResponse getAdminLectures(GetAdminLecturesQuery query);

        // 관리자가 강의 상세 정보를 조회한다.
        AdminLectureDetailResponse getAdminLectureDetail(GetAdminLectureDetailQuery query);
    }
}