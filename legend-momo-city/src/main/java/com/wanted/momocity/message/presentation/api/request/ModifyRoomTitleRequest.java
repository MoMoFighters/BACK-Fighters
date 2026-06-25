package com.wanted.momocity.message.presentation.api.request;

public record ModifyRoomTitleRequest(
        Long userId,
        Long roomId,
        String roomTitle
) {
}
