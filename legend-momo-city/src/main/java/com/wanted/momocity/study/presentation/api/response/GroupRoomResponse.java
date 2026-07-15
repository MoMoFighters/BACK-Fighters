package com.wanted.momocity.study.presentation.api.response;

/*
 * comment.
 *  그룹방 생성 응답 DTO
 *  - 사용 API : POST /api/v3/study/rooms
 *  - RoomCommandService.createRoom() 결과를 Controller가 이 Response로 조립해서 반환
 *  - inviteCode는 생성 직후에만 프론트가 알 수 있는 값이라 이 응답에 반드시 포함되어야 함
 * */

public record GroupRoomResponse(
        Long roomId,
        Long hostUserId,
        String hostNickname,
        String inviteCode,
        String status,
        int maxMember
) {
}