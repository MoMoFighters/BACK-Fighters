package com.wanted.momocity.community.presentation.api.response;

import java.time.LocalDateTime;

/*
* comment.
*  대댓글 단건 응답 DTO
*  replies, hasMoreReplies, nextReplyCursor 없음
* */

public record ReplyResponse(
        Long commentId,
        Long authorId,
        String content,
        String authorName,
        String authorProfileImageUrl,
        String authorRole,
        boolean isMine,
        boolean isPostWriter,
        LocalDateTime createdAt
) {
}
