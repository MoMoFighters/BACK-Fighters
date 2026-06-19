package com.wanted.momocity.message.domain.event;

import com.wanted.momocity.friend.user.UserWithFMJpaEntity;

import java.time.LocalDateTime;

public record SendMessagePublishedEvent(
        Long roomId,
        Long senderId,
        String senderNickname,
        UserWithFMJpaEntity receiverId, //알림 받아야 하는 사람
        LocalDateTime createdAt
) {
}
