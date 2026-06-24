package com.wanted.momocity.community.presentation.api.response;

import java.time.LocalDateTime;

public record CommentCreateResponse(
        Long commentId,
        String content,
        String authorName,
        String authorProfileImageUrl,
        String authorRole,
        LocalDateTime createdAt
) {
}
