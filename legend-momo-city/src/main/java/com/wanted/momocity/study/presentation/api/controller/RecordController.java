package com.wanted.momocity.study.presentation.api.controller;

import com.wanted.momocity.auth.infrastructure.security.CustomUserDetails;
import com.wanted.momocity.global.presentation.api.common.ApiResponse;
import com.wanted.momocity.study.application.record.usecase.RecordQueryUseCase;
import com.wanted.momocity.study.presentation.api.common.StudyResponseCode;
import com.wanted.momocity.study.presentation.api.response.record.DailyRecordResponse;
import com.wanted.momocity.study.presentation.api.response.record.MonthlyRecordResponse;
import com.wanted.momocity.study.presentation.api.response.record.RankingResponse;
import com.wanted.momocity.study.presentation.api.response.record.YearlyRecordResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;

/*
 * comment.
 *  개인 공부 기록 통계 HTTP 요청 처리
 *  - 비즈니스 로직 없음, UseCase 호출 + Response 그대로 반환만 담당
 * */

@Tag(name = "Record", description = "Study(열품타) 도메인 - 개인 공부 기록 통계 API")
@RestController
@RequiredArgsConstructor
public class RecordController {

    private final RecordQueryUseCase recordQueryUseCase;

    // 일별 누적 공부시간 조회
    @Operation(summary = "일별 공부시간 조회", description = "특정 날짜의 개인 누적 공부시간을 조회합니다.")
    @GetMapping("/api/v3/study/records/daily")
    public ResponseEntity<ApiResponse<DailyRecordResponse>> getDaily(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                StudyResponseCode.RECORD_DAILY_FETCHED,
                "일별 공부시간을 조회했습니다.",
                recordQueryUseCase.getDaily(userDetails.getUserId(), date)
        ));
    }

    // 월별 누적 공부시간 조회
    @Operation(summary = "월별 공부시간 조회", description = "특정 월의 개인 누적 공부시간을 조회합니다.")
    @GetMapping("/api/v3/study/records/monthly")
    public ResponseEntity<ApiResponse<MonthlyRecordResponse>> getMonthly(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                StudyResponseCode.RECORD_MONTHLY_FETCHED,
                "월별 공부시간을 조회했습니다.",
                recordQueryUseCase.getMonthly(userDetails.getUserId(), yearMonth)
        ));
    }

    // 연간 잔디 조회 (쿼리스트링 없음, 마이페이지 진입 시)
    @Operation(summary = "연간 잔디 조회", description = "로그인 유저 기준 최근 1년치 공부 기록을 조회합니다.")
    @GetMapping("/api/v3/study/records/yearly")
    public ResponseEntity<ApiResponse<YearlyRecordResponse>> getYearly(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                StudyResponseCode.RECORD_YEARLY_FETCHED,
                "잔디 데이터를 조회했습니다.",
                recordQueryUseCase.getYearly(userDetails.getUserId())
        ));
    }

    // 그룹방 멤버 일별 랭킹 조회
    @Operation(summary = "방 일별 랭킹 조회", description = "그룹방 멤버들의 오늘 누적 공부시간 랭킹을 조회합니다.")
    @GetMapping("/api/v3/study/rooms/{roomId}/ranking/daily")
    public ResponseEntity<ApiResponse<RankingResponse>> getDailyRanking(
            @PathVariable Long roomId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                StudyResponseCode.RANKING_DAILY_FETCHED,
                "방 일별 랭킹을 조회했습니다.",
                recordQueryUseCase.getDailyRanking(userDetails.getUserId(), roomId)
        ));
    }

    // 그룹방 멤버 월별 랭킹 조회
    @Operation(summary = "방 월별 랭킹 조회", description = "그룹방 멤버들의 이번 달 누적 공부시간 랭킹을 조회합니다.")
    @GetMapping("/api/v3/study/rooms/{roomId}/ranking/monthly")
    public ResponseEntity<ApiResponse<RankingResponse>> getMonthlyRanking(
            @PathVariable Long roomId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                StudyResponseCode.RANKING_MONTHLY_FETCHED,
                "방 월별 랭킹을 조회했습니다.",
                recordQueryUseCase.getMonthlyRanking(userDetails.getUserId(), roomId)
        ));
    }

}
