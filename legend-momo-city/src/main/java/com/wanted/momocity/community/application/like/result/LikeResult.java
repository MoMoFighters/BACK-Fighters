package com.wanted.momocity.community.application.like.result;

public record LikeResult(
        Long postId,
        int likeCount,
        boolean isLiked
) {
}
