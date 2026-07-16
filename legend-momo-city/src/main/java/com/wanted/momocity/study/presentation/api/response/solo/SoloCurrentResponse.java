package com.wanted.momocity.study.presentation.api.response.solo;

import java.time.LocalDateTime;

/*
 * comment.
 *  현재 진행 중인 솔로 세션 조회 응답 DTO
 *  - 사용 API : GET /api/v3/study/solo/current
 *  - 새로고침/재접속 시 화면 복구 용도 (동시성 제어 목적 아님)
 *  - 진행 중인 세션이 없으면 이 객체 자체가 아니라 ApiResponse.data 가 null로 내려감
 *    (Controller가 Optional 처리 - Service는 null을 반환할 수 있음)
 * */

public record SoloCurrentResponse(
        Long sessionId,
        String status,
        LocalDateTime startTime,
        int accumulatedSeconds
) {
}