package com.wanted.momocity.study.presentation.api.response.record;

/*
 * comment.
 *  월별 누적 공부시간 조회 응답 DTO
 *  - 사용 API : GET /api/v3/study/records/monthly?yearMonth=
 *  - MonthlyStudyRecord 기준, 이벤트 기반으로 실시간 누적된 값
 *  - yearMonth는 "YYYY-MM" 문자열 형식
 * */

public record MonthlyRecordResponse(
        String yearMonth,
        int totalSeconds
) {
}