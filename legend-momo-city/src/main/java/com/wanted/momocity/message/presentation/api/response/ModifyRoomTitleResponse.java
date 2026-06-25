package com.wanted.momocity.message.presentation.api.response;

import java.time.LocalDateTime;

public record ModifyRoomTitleResponse(
        Long roomId,
        Long userId,
        String nickname, //채팅방 이름 변경한 사람
        String role, //채팅방 이름 변경한 사람
        String roomTitle, //변경한 방이름
        LocalDateTime createdAt //업데이트 시간
) {

}
