package com.wanted.momocity.community.application.result;

public record LikeResult(
        Long postId,
        int likeCount,
        boolean isLiked
) {
}
