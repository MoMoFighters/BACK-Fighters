package com.wanted.momocity.study.presentation.api.response.room;

/*
 * comment.
 *  그룹방 생성 응답 DTO
 *  - 사용 API : POST /api/v3/study/rooms
 *  - RoomCommandService.createRoom() 결과를 Controller가 이 Response로 조립해서 반환
 * */

public record GroupRoomResponse(
        Long roomId,
        Long hostUserId,
        String hostNickname,
        String status,
        int maxMember
) {
}