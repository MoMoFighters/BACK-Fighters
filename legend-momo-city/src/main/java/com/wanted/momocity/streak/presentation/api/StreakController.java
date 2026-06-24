package com.wanted.momocity.streak.presentation.api;

import com.wanted.momocity.auth.infrastructure.security.CustomUserDetails;
import com.wanted.momocity.global.presentation.api.common.ApiResponse;
import com.wanted.momocity.streak.application.usecase.StreakQueryUseCase;
import com.wanted.momocity.streak.presentation.api.common.StreakResponseCode;
import com.wanted.momocity.streak.presentation.api.response.StreakMonthlyResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Tag(name = "Streak", description = "Streak 도메인 - 잔디 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2")
@Validated
public class StreakController {

    private final StreakQueryUseCase streakQueryUseCase;

    @Operation(summary =  "잔디 월간 조회",
    description = "메인 페이지 진입 시 한달 치 잔디 조회. 시청기록 없는 날짜는 포함 안 함")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 토큰 만료")
    })
    @GetMapping("/streak")
    public ResponseEntity<ApiResponse<StreakMonthlyResponse>> getMonthlyStreak(
            @RequestParam @Min(2000) @Max(2100) int year,
            @RequestParam @Min(1) @Max(12) int month,
            @AuthenticationPrincipal CustomUserDetails userDetails
            ) {

        Long userId =userDetails.getUserId();

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        return ResponseEntity.ok(ApiResponse.success(
                StreakResponseCode.STREAK_MONTHLY_FOUND,
                "잔디 월간 조회에 성공했습니다.",
                streakQueryUseCase.getMonthlyStreak(userId, startDate, endDate)
        ));

    }

}
