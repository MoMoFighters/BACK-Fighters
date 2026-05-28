package com.wanted.momocity.message.presentation.api.response;

import java.time.LocalDateTime;

public record SendMessageResponse(
        Long roomId,
        Long userId,
        String nickname,
        String role,
        String status,
        String content,
        LocalDateTime createdAt
) {
}
