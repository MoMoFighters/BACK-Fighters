package com.wanted.momocity.study.presentation.api.response.record;

import java.time.LocalDate;
import java.util.List;

/*
 * comment.
 *  잔디(연간) 조회 응답 DTO
 *  - 사용 API : GET /api/v3/study/records/yearly
 *  - 마이페이지 진입 시 호출 (기존 Streak의 /api/v2/streak/yearly와 동일한 진입 지점 패턴)
 *  - 쿼리스트링 없이 로그인 유저 기준 최근 1년치 전체를 반환
 *  - 기존 영상 시청 기반 Streak와는 별개의 잔디 (DailyStudyRecord 기반, 열품타 전용)
 * */

public record YearlyRecordResponse(
        List<DayRecord> records
) {
    public record DayRecord(
            LocalDate date,
            int totalSeconds
    ) {}
}