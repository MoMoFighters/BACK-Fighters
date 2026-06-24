package com.wanted.momocity.community.domain.event;

public record PostLikedEvent(
        Long postId,
        // 알림 받을 사람 (게시글 작성자)
        Long postOwnerId,
        // 좋아요 누른 사람
        Long likeUserId,
        String likedUserName
) {
}
