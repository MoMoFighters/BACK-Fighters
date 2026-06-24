package com.wanted.momocity.community.application.result;

import java.time.LocalDateTime;

public record ReplyCreateResult(
        Long replyId,
        Long commentId,
        String content,
        String authorName,
        String authorProfileImageUrl,
        String authorRole,
        LocalDateTime createdAt
) {
}
