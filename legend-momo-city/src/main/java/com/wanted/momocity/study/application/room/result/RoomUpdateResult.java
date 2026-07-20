package com.wanted.momocity.study.application.room.result;

/*
 * comment.
 *  그룹방 제목 수정 결과 DTO
 *  -> Service가 반환 -> Controller가 응답으로 그대로 조립
 * */

public record RoomUpdateResult(
        Long roomId,
        String title
) {
}
