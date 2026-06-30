package com.wanted.momocity.admin.application.service;

import com.wanted.momocity.admin.application.port.LectureStatsPort;
import com.wanted.momocity.admin.application.port.MemberStatsPort;
import com.wanted.momocity.admin.application.port.MonthlyCount;
import com.wanted.momocity.admin.application.port.PostStatsPort;
import com.wanted.momocity.admin.application.usecase.MonthlyStatsQueryUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
        // 조회 연도가 올해면 현재 월까지만, 과거 연도면 12월까지 포함
        int currentMonth = (LocalDate.now().getYear() == year)
                ? LocalDate.now().getMonthValue()
                : 12;

        // 3개 BC 포트에서 월별 원시 데이터를 받아 누적값으로 변환 후 반환
        return new MonthlyStats(
                toCumulative(memberStatsPort.countMemberByMonth(year), currentMonth),
                toCumulative(lectureStatsPort.countLectureByMonth(year), currentMonth),
                toCumulative(postStatsPort.countPostByMonth(year), currentMonth)
        );
    }
    private List<MonthlyCount> toCumulative(List<MonthlyCount> raw, int upToMonth) {
        // 누적 합계를 담는 변수
        long running = 0;
        // 누적 변환 결과를 담을 리스트
        List<MonthlyCount> result = new ArrayList<>();
        for (MonthlyCount mc : raw) {
            // 현재 월 초과 시 미래 달 데이터 제거
            if (mc.month() > upToMonth) break;
            // 이전 달 합계에 이번 달 수치를 더해 누적시키기
            running += mc.count();
            // 해당 월 + 누적값으로 새 객체 생성 후 추가
            result.add(new MonthlyCount(mc.month(), running));
        }
        return result;
    }

}
