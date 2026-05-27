package com.wanted.momocity.enrollment.application.port;

import com.wanted.momocity.lecture.domain.model.LectureStatus;

// 수강신청할 강의 정보를 확인하기 위한 포트
public interface EnrollmentLecturePort {

    // 강의 ID로 강의 상태를 조회
    LectureStatus getLectureStatus(Long lectureId);
}