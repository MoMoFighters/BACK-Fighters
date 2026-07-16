package com.wanted.momocity.study.application.room.result;

/*
 * comment.
 *  그룹방 생성 결과 DTO
 *  -> Service가 반환 -> Controller가 GroupRoomResponse로 조립
 * */

public record RoomCreateResult(
        Long roomId,
        Long hostUserId,
        String hostNickname,
        String status,
        int maxMember
) {
}