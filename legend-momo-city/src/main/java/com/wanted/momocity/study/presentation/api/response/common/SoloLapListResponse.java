package com.wanted.momocity.study.presentation.api.response.common;

import java.util.List;

/*
 * comment.
 *  솔로 세션 랩 목록 조회 응답 DTO
 *  - 사용 API : GET /api/v3/study/solo/laps
 *  - 로그인한 유저의 현재(또는 가장 최근) 솔로 세션 하나의 랩 목록만 반환
 *  - LapItem은 solo/member.timer 양쪽이 공유하는 공용 레코드 (presentation.api.response.common)
 * */

public record SoloLapListResponse(
        Long sessionId,
        List<LapItem> laps
) {
}
