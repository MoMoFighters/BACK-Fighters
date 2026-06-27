package com.wanted.momocity.friend.domain.event;

import java.time.LocalDateTime;

public record RegisterGuestBookPublishedEvent(
        Long bookId,
        Long writerId,
        String writerNickname,
        Long ownerId,
        LocalDateTime now
) {
}
