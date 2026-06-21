package com.wanted.momocity.community.presentation.api.response;

public record LikeResponse(
        Long postId,
        int likeCount,
        boolean isLiked
) {
}
