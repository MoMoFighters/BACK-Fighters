package com.wanted.momocity.study.presentation.api.response;

import java.time.LocalDateTime;
import java.util.List;

/*
 * comment.
 *  솔로 세션 이력(종료된 세션들) 조회 응답 DTO
 *  - 사용 API : GET /api/v3/study/solo/history
 *  - status=ENDED인 세션만 최신순으로 반환 (진행 중인 세션은 GET /solo/current로 별도 조회)
 *  - 페이지네이션 파라미터(cursor 등)는 추후 확정 필요 - 현재는 전체 반환 가정
 * */

public record SoloHistoryResponse(
        List<SessionItem> sessions
) {
    public record SessionItem(
            Long sessionId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            int totalSeconds
    ) {}
}