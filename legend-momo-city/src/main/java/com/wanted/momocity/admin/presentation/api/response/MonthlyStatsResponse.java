package com.wanted.momocity.admin.presentation.api.response;

import com.wanted.momocity.admin.application.port.MonthlyCount;
import com.wanted.momocity.admin.application.usecase.MonthlyStatsQueryUseCase.MonthlyStats;

import java.util.List;

// 대시보드 월별 운영 추이 그래프 응답 DTO
public record MonthlyStatsResponse(
        List<MonthlyCount> memberCounts,
        List<MonthlyCount> lectureCounts,
        List<MonthlyCount> postCounts
) {
    public static MonthlyStatsResponse from(MonthlyStats stats) {
        return new MonthlyStatsResponse(
                stats.memberCounts(),
                stats.lectureCounts(),
                stats.postCounts()
        );
    }
}
