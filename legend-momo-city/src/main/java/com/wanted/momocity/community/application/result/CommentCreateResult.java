package com.wanted.momocity.community.application.result;

import java.time.LocalDateTime;

public record CommentCreateResult(
        Long commentId,
        String content,
        String authorName,
        String authorProfileImageUrl,
        String authorRole,
        LocalDateTime createdAt
) {
}
