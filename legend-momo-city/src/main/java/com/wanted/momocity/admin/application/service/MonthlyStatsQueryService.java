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

        // 해당 연도 1월 1일 이전의 누적 기준점 (연도 간 연속성 보장)
        LocalDate baselineDate = LocalDate.of(year, 1, 1);
        long memberBaseline  = memberStatsPort.countActiveBefore(baselineDate);
        long lectureBaseline = lectureStatsPort.countActiveBefore(baselineDate);
        long postBaseline    = postStatsPort.countPostBefore(baselineDate);

        // 3개 BC 포트에서 월별 원시 데이터를 받아 기준점 + 누적값으로 변환 후 반환
        return new MonthlyStats(
                toCumulative(memberStatsPort.countMemberByMonth(year), currentMonth, memberBaseline),
                toCumulative(lectureStatsPort.countLectureByMonth(year), currentMonth, lectureBaseline),
                toCumulative(postStatsPort.countPostByMonth(year), currentMonth, postBaseline)
        );
    }

    // year 가 올해라면 현재 월까지만 값을 보내줌
    // year 가 과거라면 12월까지 전부 포함해서 보내줌
    @Override
    public MonthlyStats getMonthlyNewStats(int year) {
        int currentMonth = (LocalDate.now().getYear() == year)
                ? LocalDate.now().getMonthValue()
                : 12;

        // 포트 3개에서 각각 해당 연도 월별 신규 데이터 가져옴
        // trimFuture 로 미래 달을 잘라낸 뒤 MonthlyStats 로 묶어서 반환
        return new MonthlyStats(
                trimFuture(memberStatsPort.countMemberByMonth(year), currentMonth),
                trimFuture(lectureStatsPort.countLectureByMonth(year), currentMonth),
                trimFuture(postStatsPort.countPostByMonth(year), currentMonth)
        );
    }

    private List<MonthlyCount> toCumulative(List<MonthlyCount> raw, int upToMonth, long baseline) {
        // 이전 연도까지의 누적값을 시작점으로 설정
        long running = baseline;
        // 누적 변환 결과를 담을 리스트
        List<MonthlyCount> result = new ArrayList<>();
        for (MonthlyCount mc : raw) {
            // 현재 월 초과 시 미래 달 데이터 제거
            if (mc.month() > upToMonth) break;
            // 이전 달 합계에 이번 달 수치를 더해 누적
            running += mc.count();
            // 해당 월 + 누적값으로 새 객체 생성 후 추가
            result.add(new MonthlyCount(mc.month(), running));
        }
        return result;
    }


    private List<MonthlyCount> trimFuture(List<MonthlyCount> raw, int upToMonth) {
        //포트에서 받은 1~12월 리스트를 그대로 쓰되, currentMonth 초과 달만 제거
        return raw.stream()
                .filter(mc -> mc.month() <= upToMonth)
                .toList();
    }

}
