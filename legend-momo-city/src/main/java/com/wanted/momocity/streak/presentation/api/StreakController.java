package com.wanted.momocity.streak.presentation.api;

import com.wanted.momocity.auth.infrastructure.security.CustomUserDetails;
import com.wanted.momocity.global.presentation.api.common.ApiResponse;
import com.wanted.momocity.streak.application.usecase.StreakQueryUseCase;
import com.wanted.momocity.streak.presentation.api.common.StreakResponseCode;
import com.wanted.momocity.streak.presentation.api.response.StreakMonthlyResponse;
import com.wanted.momocity.streak.presentation.api.response.StreakYearlyResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "Streak", description = "Streak 도메인 - 잔디 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2")
@Validated
public class StreakController {

    private final StreakQueryUseCase streakQueryUseCase;

    // 잔디 월간 조회
    // GET /api/v2/streak
    @Operation(summary =  "잔디 월간 조회",
    description = "메인 페이지 진입 시 한달 치 잔디 조회. 시청기록 없는 날짜는 포함 안 함")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 토큰 만료")
    })
    @GetMapping("/streak")
    public ResponseEntity<ApiResponse<StreakMonthlyResponse>> getMonthlyStreak(
            @AuthenticationPrincipal CustomUserDetails userDetails
            ) {

        Long userId =userDetails.getUserId();

        // 월간 -> 서버에서 현재 년/월 계산
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.withDayOfMonth(1);
        LocalDate endDate = now.withDayOfMonth(now.lengthOfMonth());

//        LocalDate startDate = LocalDate.of(year, month, 1);
//        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        return ResponseEntity.ok(ApiResponse.success(
                StreakResponseCode.STREAK_MONTHLY_FOUND,
                "잔디 월간 조회에 성공했습니다.",
                streakQueryUseCase.getMonthlyStreak(userId, startDate, endDate)
        ));

    }

    // 연간 잔디 조회
    // GET /api/v2/streak/yearly
    @Operation(summary = "연간 잔디 조회",
            description = "마이페이지 진입 시 해당 년도 전체 잔디 조회. 시청기록 없는 날짜는 포함 안 함")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 토큰 만료")
    })
    @GetMapping("/streak/yearly")
    public ResponseEntity<ApiResponse<StreakYearlyResponse>> getYearlyStreak(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUserId();

        // 연간 -> 서버에서 현재 년도 계산
        int year = LocalDate.now().getYear();

        return ResponseEntity.ok(ApiResponse.success(
                StreakResponseCode.STREAK_YEARLY_FOUND,
                "연간 잔디 조회에 성공했습니다.",
                streakQueryUseCase.getYearlyStreak(userId, year)
        ));
    }

    // 친구 잔디 월간 조회
    // GET /api/v2/streak/users/{targetUserId}
    @Operation(summary = "친구 잔디 월간 조회",
            description = "친구 도시 방문 시 친구의 한달치 잔디 조회.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 토큰 만료")
    })
    @GetMapping("/streak/users/{targetUserId}")
    public ResponseEntity<ApiResponse<StreakMonthlyResponse>> getFriendMonthlyStreak(
            @PathVariable Long targetUserId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
//        LocalDate startDate = LocalDate.of(year, month, 1);
//        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        // 월간 -> 서버에서 현재 년/월 계산
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.withDayOfMonth(1);
        LocalDate endDate = now.withDayOfMonth(now.lengthOfMonth());

        return ResponseEntity.ok(ApiResponse.success(
                StreakResponseCode.STREAK_MONTHLY_FOUND,
                "친구 잔디 월간 조회에 성공했습니다.",
                streakQueryUseCase.getFriendMonthlyStreak(targetUserId, startDate, endDate)
        ));
    }

}
