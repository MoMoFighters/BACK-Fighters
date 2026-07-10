package com.wanted.momocity.streak.presentation.api.response;

import java.time.LocalDate;
import java.util.List;

/*
* comment.
*  월간 잔디 조회 응답 DTO
*  - streaks : 날짜별 잔디 목록
*  - 시청 기록 없는 날짜는 포함 안 함
* */

public record StreakMonthlyResponse(
        List<StreakResponse> streaks
) {
}
