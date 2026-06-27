package com.wanted.momocity.community.presentation.api.response;

import java.time.LocalDateTime;
import java.util.List;

/*
* comment.
*  게시글 목록 조회 응답 DTO
*  - totalCount : 젠체 게시글 수 (키테고리 필터 적용)
*  - posts : 게시글 목록,
*  - nextCursor : 다음 페이지 커서 (없으면 null -> 마지막 페이지)
* */

public record PostListResponse (
        int totalCount,
        List<PostItem> posts,
        Long nextCursor
) {

    /*
    * comment.
    *  PostItem : 게시글 목록 단건 응답 DTO
    *  - 목록에서는 contents 미포함
    * */

    public record PostItem(
            Long postId,
            String title,
            String category,
            int viewCount,
            int likeCount,
            int commentCount,
            String thumbnailUrl,
            Long authorId,
            String authorName,
            String authorProfileImageUrl,
            String authorRole,
            LocalDateTime createdAt
    ) {}

}
