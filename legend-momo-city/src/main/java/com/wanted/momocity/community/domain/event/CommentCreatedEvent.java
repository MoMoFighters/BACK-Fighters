package com.wanted.momocity.community.domain.event;

public record CommentCreatedEvent(
        Long postId,
        // 알림 받을 사람 (게시글 작성자)
        Long postOwnerId,
        // 댓글 작성자
        Long commentUserId,
        String commentUserName
) {
}
