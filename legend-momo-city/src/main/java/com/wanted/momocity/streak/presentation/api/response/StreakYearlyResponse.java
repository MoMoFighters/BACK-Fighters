package com.wanted.momocity.streak.presentation.api.response;

import java.util.List;

/*
* comment.
*  연간 잔디 조회 응답 DTO
*  -> 마이페이지 진입 시 해당 년도 전체 잔디 조회
*  -> 시청 기록 없는 날짜는 응답에 포함 안 함
* */

public record StreakYearlyResponse(List<StreakResponse> streaks) {
}
