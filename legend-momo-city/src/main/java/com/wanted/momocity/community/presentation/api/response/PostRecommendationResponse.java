package com.wanted.momocity.community.presentation.api.response;

import java.time.LocalDateTime;
import java.util.List;

/*
* comment.
*  연관 게시글 추천 응답 DTO
*  - TopPosts : 같은 카테고리 내 인기글
*  -> (viewCount * 0.6 + likeCount * 0.4) 가중치 합산 순, 최대 3개
*  - authorPosts : 같은 작성자의 최신 게시글
*  -> 최대 2개, topPosts 와 중복 제외
* */

public record PostRecommendationResponse(
        List<RecommendItem> topPosts,
        List<RecommendItem> authorPosts
) {

    /*
     * comment.
     *  RecommendItem : 추천 게시글 단건 응답 DTO
     */
    public record RecommendItem(
            Long postId,
            String title,
            String category,
            int viewCount,
            int likeCount,
            int commentCount,
            String thumbnailUrl,
            Long authorId,
            String authorName,
            LocalDateTime createdAt
    ) {}

}
