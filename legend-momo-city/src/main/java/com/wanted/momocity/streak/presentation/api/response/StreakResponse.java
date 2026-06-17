package com.wanted.momocity.streak.presentation.api.response;

import java.time.LocalDate;

/*
* comment.
*  날짜별 잔디 단건 응답 DTO
*  - streakDate : 학습 날짜
*  - dailyWatchedSeconds : 해당 날짜 총 시청 시간
*  - level : 시청 시간 기반 레벨 (0-4)
* */

public record StreakResponse(
        LocalDate streakDate,
        int dailyWatchedSeconds,
        int level
) {
}
