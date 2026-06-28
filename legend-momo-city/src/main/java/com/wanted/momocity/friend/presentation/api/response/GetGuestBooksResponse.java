package com.wanted.momocity.friend.presentation.api.response;

import java.time.LocalDateTime;

public record GetGuestBooksResponse(
        Long bookId,
        Long writerId,
        String nickname, //방명록 작성자 닉네임
        String content,
        LocalDateTime createdAt
) {
}
