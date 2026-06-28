package com.wanted.momocity.user.application.port;

public interface ReportRedisPort {

    // 신고 시각 저장 (TTL 24시간)
    void saveReportTime(Long userId);

    // 24시간 이내 신고 기록 있는지 확인
    boolean existsReportTime(Long userId);

    // 신고 복구 후 키 삭제
    void deleteReportTime(Long userId);
}