package com.wanted.momocity.study.presentation.api.response.room;

/*
* comment.
*  그룹방 제목 수정 응답 DTO
*  - 사용 API : PATCH /api/v3/study/rooms/{roomId}
* */

public record RoomUpdateResponse(
        Long roomId,
        String title
) {
}
