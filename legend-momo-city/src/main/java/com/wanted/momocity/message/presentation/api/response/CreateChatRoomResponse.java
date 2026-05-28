package com.wanted.momocity.message.presentation.api.response;

public record CreateChatRoomResponse(
        Long roomId,
        Long userId,
        String nickname,
        String role,
        String status
) {
}
