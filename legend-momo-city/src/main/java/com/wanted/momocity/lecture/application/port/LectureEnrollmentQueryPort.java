package com.wanted.momocity.lecture.application.port;

import java.util.List;
import java.util.Optional;

// LectureEnrollmentQueryPort는 lecture 조회에서 필요한 enrollment 정보를 가져오기 위한 포트
public interface LectureEnrollmentQueryPort {

    // 사용자가 수강 신청한 강의 ID 목록을 조회
    List<Long> findLectureIdsByUserId(Long userId);

    // 특정 사용자의 특정 강의 수강 신청 정보를 조회
    Optional<EnrollmentProgress> findByUserIdAndLectureId(Long userId, Long lectureId);

    // 강의 목록 응답에 필요한 수강 진행 정보
    record EnrollmentProgress(
            Long enrollmentId,
            Long lectureId,
            int totalProgress,
            int completedCount
    ) {
    }
}