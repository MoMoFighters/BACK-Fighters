package com.wanted.momocity.study.application.record.usecase;

import com.wanted.momocity.study.presentation.api.response.record.DailyRecordResponse;
import com.wanted.momocity.study.presentation.api.response.record.MonthlyRecordResponse;
import com.wanted.momocity.study.presentation.api.response.record.RankingResponse;
import com.wanted.momocity.study.presentation.api.response.record.YearlyRecordResponse;

import java.time.LocalDate;
import java.time.YearMonth;

/*
 * comment.
 *  개인 공부 기록 통계 읽기 작업 전용 UseCase 인터페이스
 *  - 일별/월별/연간(잔디) 조회, 방 랭킹(일별/월별) 조회
 * */

public interface RecordQueryUseCase {

    // 특정 날짜 개인 누적시간 조회 (잔디 상세용)
    DailyRecordResponse getDaily(Long userId, LocalDate date);

    // 특정 월 개인 누적시간 조회
    MonthlyRecordResponse getMonthly(Long userId, YearMonth yearMonth);

    // 연간 잔디 조회 (쿼리스트링 없이 최근 1년, 마이페이지 진입 시)
    YearlyRecordResponse getYearly(Long userId);

    // 그룹방 멤버 일별 랭킹 조회
    RankingResponse getDailyRanking(Long userId, Long roomId);

    // 그룹방 멤버 월별 랭킹 조회
    RankingResponse getMonthlyRanking(Long userId, Long roomId);

}
