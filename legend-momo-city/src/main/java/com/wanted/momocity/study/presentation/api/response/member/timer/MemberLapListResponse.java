package com.wanted.momocity.study.presentation.api.response.member.timer;

import com.wanted.momocity.study.presentation.api.response.common.LapItem;

import java.util.List;

/*
 * comment.
 *  그룹방 멤버 랩 목록 조회 응답 DTO
 *  - 사용 API : GET /api/v3/study/rooms/{roomId}/members/{targetUserId}/laps
 *  - 그 멤버의 현재(또는 가장 최근) group_room_member 기준 랩 목록만 반환
 *  - LapItem은 solo/member.timer 양쪽이 공유하는 공용 레코드 (presentation.api.response.common)
 * */

public record MemberLapListResponse(
        Long targetUserId,
        List<LapItem> laps
) {
}
