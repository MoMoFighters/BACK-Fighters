package com.wanted.momocity.admin.application.service;

import com.wanted.momocity.admin.application.port.LectureStatsPort;
import com.wanted.momocity.admin.application.port.MemberStatsPort;
import com.wanted.momocity.admin.application.port.PostStatsPort;
import com.wanted.momocity.admin.application.usecase.MonthlyStatsQueryUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 대시보드 월별 운영 추이 — 3개 BC 포트에서 집계 후 반환
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MonthlyStatsQueryService implements MonthlyStatsQueryUseCase {

    private final MemberStatsPort memberStatsPort;
    private final LectureStatsPort lectureStatsPort;
    private final PostStatsPort postStatsPort;

    @Override
    public MonthlyStats getMonthlyStats(int year) {
        return new MonthlyStats(
                memberStatsPort.countMemberByMonth(year),
                lectureStatsPort.countLectureByMonth(year),
                postStatsPort.countPostByMonth(year)
        );
    }
}
