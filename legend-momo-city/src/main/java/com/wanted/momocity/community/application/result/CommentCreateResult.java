package com.wanted.momocity.community.application.result;

import java.time.LocalDateTime;

public record CommentCreateResult(
        Long commentId,
        Long authorId,
        String content,
        String authorName,
        String authorProfileImageUrl,
        String authorRole,
        LocalDateTime createdAt
) {
}
