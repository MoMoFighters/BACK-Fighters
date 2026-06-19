package com.wanted.momocity.streak.presentation.api.response;

import com.wanted.momocity.streak.domain.model.StreakLevel;

import java.time.LocalDate;

/*
* comment.
*  날짜별 잔디 단건 응답 DTO
*  - streakDate : 학습 날짜
*  - level : 시청 시간 기반 레벨 (0-4)
* */

public record StreakResponse(
        LocalDate streakDate,
        StreakLevel level
) {
}
