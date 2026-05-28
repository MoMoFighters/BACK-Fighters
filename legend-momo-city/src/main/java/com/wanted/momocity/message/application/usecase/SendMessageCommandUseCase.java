package com.wanted.momocity.message.application.usecase;

import java.time.LocalDateTime;

public interface SendMessageCommandUseCase {
    SendView handle(Long senderId, Long roomId, String content);

    record SendView(
            Long roomId,
            Long targetUserId,
            String targetNickname,
            String targetRole,
            String friendStatus,
            String content,
            LocalDateTime createdAt
    ) {}
}
