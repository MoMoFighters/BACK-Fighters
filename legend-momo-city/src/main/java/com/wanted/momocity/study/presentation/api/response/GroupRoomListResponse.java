package com.wanted.momocity.study.presentation.api.response;

import java.util.List;

/*
 * comment.
 *  내가 속한 그룹방 목록 응답 DTO
 *  - 사용 API : GET /api/v3/study/rooms/my
 *  - 마이페이지 또는 열품타 홈에서 "내가 참여 중인 방" 리스트를 보여줄 때 사용
 *  - status=JOINED인 방만 포함 (ENDED되어 소프트딜리트된 방은 제외)
 *  - 상세 멤버 목록까지는 필요 없고 memberCount(숫자)만 필요한 화면이라 GroupRoomDetailResponse와 분리
 * */

public record GroupRoomListResponse(
        List<RoomItem> rooms
) {
    public record RoomItem(
            Long roomId,
            Long hostUserId,
            String hostNickname,
            int memberCount,
            String status
    ) {}
}