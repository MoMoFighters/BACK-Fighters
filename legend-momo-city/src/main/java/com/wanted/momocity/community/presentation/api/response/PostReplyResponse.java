package com.wanted.momocity.community.presentation.api.response;

/*
* comment.
*  대댓글 목록 조회 전용 응답 DTO
*  - ReplyResponse 사용 (replies, hasMoreReplies, nextReplyCursor 없음)
* */

import java.util.List;

public record PostReplyResponse(
        int totalCount,
        List<ReplyResponse> replies,
        Long nextCursor
) {
}
