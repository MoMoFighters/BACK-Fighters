package com.wanted.momocity.community.presentation.api.response;

import java.util.List;

/*
* comment.
*  게시글 댓글 목록 조회 응답 DTO
*  - 커서기반 페이지네이션
*  cursor : 마지막으로 조회한 commentId
*  nextCursor : 다음 페이지 커서 (없으면 null)
*  -
*  totalCount : 전체 댓글 수 (대댓글 제외)
* */

public record PostCommentResponse(
        int totalCount,
        List<CommentResponse> comments,
        Long nextCursor
) {
}
