package com.wanted.momocity.enrollment.application.port;

// Enrollment가 Viewing의 진척도 정보를 조회하기 위해 사용하는 Port.
public interface ProgressPort {

    // 기존 totalProgress만 필요한 코드 호환용
    default int getTotalProgress(Long userId, Long lectureId) {
        return getProgress(userId, lectureId).totalProgress();
    }

    // viewing 기준 전체 진도율 + 완료 챕터 수를 함께 조회
    ProgressInfo getProgress(Long userId, Long lectureId);

    record ProgressInfo(
            int totalProgress,
            int completedCount
    ) {
    }
}