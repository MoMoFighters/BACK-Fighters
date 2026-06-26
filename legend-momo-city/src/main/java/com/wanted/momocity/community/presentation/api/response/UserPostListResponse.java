package com.wanted.momocity.community.presentation.api.response;

import java.time.LocalDateTime;
import java.util.List;

/*
* comment.
*  마이페이지 / 상대방 페이지 게시글 목록 공용 응답
*  -
*  커서 기반 페이지네이션
*  nextCursor : 다음 페이지 커서 (없으면 null)
* */

public record UserPostListResponse(
        int totalCount,
        List<UserPostItem> posts,
        Long nextCursor
) {

    public record UserPostItem(
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
