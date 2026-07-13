package com.wanted.momocity.study.presentation.api.response;

import java.time.LocalDate;

/*
 * comment.
 *  일별 누적 공부시간 조회 응답 DTO
 *  - 사용 API : GET /api/v3/study/records/daily?date=
 *  - DailyStudyRecord 기준, 솔로+그룹 통합 누적값 (방 구분 없음)
 *  - 기록이 없는 날짜는 404가 아니라 totalSeconds=0으로 정상 응답
 * */

public record DailyRecordResponse(
        LocalDate date,
        int totalSeconds
) {
}