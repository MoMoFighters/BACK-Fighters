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
        Long authorId,
        String content,
        String authorName,
        String authorProfileImageUrl,
        String authorRole,
        // 내가 작성한 댓글인지 확인
        boolean isMine,
        // 게시글 작성자가 작성한 댓글인지 확인
        boolean isPostWriter,
        LocalDateTime createdAt,
        List<CommentResponse> replies,
        // 대댓글 추가 여부 (5개 초과 시 true)
        boolean hasMoreReplies,
        // 다음 대댓글 커서 (없으면 null)
        Long nextReplyCursor

) {
}
