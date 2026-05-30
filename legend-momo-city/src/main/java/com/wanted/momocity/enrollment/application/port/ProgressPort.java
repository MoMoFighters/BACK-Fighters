package com.wanted.momocity.enrollment.application.port;

// Enrollment가 Viewing의 진척도 정보를 조회하기 위해 사용하는 Port.
public interface ProgressPort {

    // 특정 사용자가 특정 강의를 얼마나 시청했는지 전체 진척도를 조회
    int getTotalProgress(Long userId, Long lectureId);
}