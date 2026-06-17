package com.wanted.momocity.streak.presentation.api.response;

import java.time.LocalDate;
import java.util.List;

/*
* comment.
*  월간 잔디 조회 응답 DTO
*  - startDate : 조회 시작 날짜
*  - endDate : 조회 종료 날짜
*  - streaks : 날짜별 잔디 목록
*  - 시청 기록 없는 날짜는 포함 안 함
* */

public record StreakMonthlyResponse(
        LocalDate startDate,
        LocalDate endDate,
        List<StreakResponse> streaks
) {
}
