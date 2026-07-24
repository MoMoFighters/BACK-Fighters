package com.wanted.momocity.study.presentation.api.response.record;

import java.util.List;

/*
 * comment.
 *  그룹방 멤버 랭킹 조회 응답 DTO (일별/월별 공용)
 *  - 사용 API :
 *      GET /api/v3/study/rooms/{roomId}/ranking/daily
 *      GET /api/v3/study/rooms/{roomId}/ranking/monthly
 *  -
 *  period : daily 조회 시 "2026-07-13"(날짜), monthly 조회 시 "2026-07"(년월) 문자열
 *  ranking : 방별 누적이 아니라 각 멤버의 "개인 전체 누적시간"(DailyStudyRecord/MonthlyStudyRecord) 기준
 *            (방 안에서 발생한 시간만 따로 집계하지 않기로 확정)
 *  - status=JOINED인 현재 멤버만 포함, 강퇴/퇴장한 멤버는 랭킹에서 제외 (개인 통계 자체는 유지됨)
 * */

public record RankingResponse(
        String period,
        List<RankingItem> ranking
) {
    public record RankingItem(
            int rank,
            Long userId,
            String nickname,
            String profileImageUrl,
            int totalSeconds
    ) {}
}