package com.wanted.momocity.community.presentation.api.response;

import java.time.LocalDateTime;
import java.util.List;

/*
* comment.
*  게시글 단건 조회 응답 DTO
*  - contents : 콘텐츠 목록 (orderNo 기준 정렬)
*  - comments : 댓글 목록 (대댓글 포함)
*  - isMins : 본인 게시글 여부
*  - isLiked : 좋아요 여부
* */

public record PostDetailResponse (
        Long postId,
        String title,
        String category,
        int viewCount,
        int likeCount,
        int commentCount,
        boolean isLiked,
        boolean isMine,
        Long authorId,
        String authorName,
        String authorProfileImageUrl,
        String authorRole,
        List<PostContentResponse> contents,
        LocalDateTime createdAt
) {
}
