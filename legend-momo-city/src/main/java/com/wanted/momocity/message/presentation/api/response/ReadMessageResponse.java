package com.wanted.momocity.message.presentation.api.response;

public record ReadMessageResponse(
        Long roomId,
        Long targetUserId,
        String nickname,
        boolean isRead
) {
}
