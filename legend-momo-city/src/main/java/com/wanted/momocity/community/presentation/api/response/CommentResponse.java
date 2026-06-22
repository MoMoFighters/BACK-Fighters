package com.wanted.momocity.community.presentation.api.response;

import java.time.LocalDateTime;
import java.util.List;

/*
* comment.
*  댓글 단건 응답 DTO
*  - replies : 대댓글 목록
* */

public record CommentResponse(
        Long commentId,
        String content,
        String authorName,
        String authorProfileImageUrl,
        String authorRole,
        LocalDateTime createdAt,
        List<CommentResponse> replies
) {
}
