package com.wanted.momocity.friend.presentation.api.response;

import java.time.LocalDateTime;

public record RegisterGuestBookResponse(
        Long bookId,
        Long ownerId,
        String nickname, //도시 주인의 닉네임
        String content,
        LocalDateTime createdAt

) {
}
