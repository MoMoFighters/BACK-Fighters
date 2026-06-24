package com.wanted.momocity.streak.application.usecase;

import com.wanted.momocity.streak.presentation.api.response.StreakMonthlyResponse;

import java.time.LocalDate;

/*
* comment.
*  잔디 월간 조회 UseCase 인터페이스
*  -> 메인 페이지 진입 시 한달치 잔디 조회
* */

public interface StreakQueryUseCase {
    StreakMonthlyResponse getMonthlyStreak(Long userId, LocalDate startDate, LocalDate endDate);
}
