package com.wanted.momocity.message.presentation.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LeaveChatRoomResponse(
        Long roomId,
        Long userId, //채팅방에 남겨진 사람
        String nickname, //채팅방에 남겨진 사람
        String role,
        String status //친구 관계
) {
}
