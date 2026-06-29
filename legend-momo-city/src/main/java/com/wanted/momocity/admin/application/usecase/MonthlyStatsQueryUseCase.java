package com.wanted.momocity.admin.application.usecase;

import com.wanted.momocity.admin.application.port.MonthlyCount;

import java.util.List;

// 대시보드 월별 운영 추이 그래프 조회 유스케이스
public interface MonthlyStatsQueryUseCase {

    MonthlyStats getMonthlyStats(int year);

    record MonthlyStats(
            List<MonthlyCount> memberCounts,
            List<MonthlyCount> lectureCounts,
            List<MonthlyCount> postCounts
    ) {}
}
