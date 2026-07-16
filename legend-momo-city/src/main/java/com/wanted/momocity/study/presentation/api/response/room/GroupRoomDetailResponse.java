package com.wanted.momocity.study.presentation.api.response.room;

import java.util.List;

/*
 * comment.
 *  그룹방 상세 조회 응답 DTO
 *  - 사용 API : GET /api/v3/study/rooms/{roomId}
 *  - 방 입장/새로고침 시 초기 화면을 그리기 위한 스냅샷 조회 용도
 *    (이후 실시간 변화는 STOMP /sub/study/room/{roomId} 구독으로 반영, 이 API는 초기 1회만 호출)
 *  - members는 status=JOINED인 현재 참가자만 포함 (LEFT/INVITED/KICKED 등은 제외)
 * */

public record GroupRoomDetailResponse(
        Long roomId,
        Long hostUserId,
        String hostNickname,
        String title,
        String status,
        int maxMember,
        List<MemberItem> members
) {
    public record MemberItem(
            Long userId,
            String nickname,
            String status,
            String timerStatus,
            int totalSeconds
    ) {}
}