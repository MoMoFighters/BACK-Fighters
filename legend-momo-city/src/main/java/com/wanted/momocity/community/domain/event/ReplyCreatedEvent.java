package com.wanted.momocity.community.domain.event;

public record ReplyCreatedEvent(
        Long postId,
        // 알림 받을 사람 (게시글 작성자)
        Long postOwnerId,
        // 알림 받을 사람 (부모 댓글 작성자)
        Long parentCommentOwnerId,
        // 대댓글 작성
        Long replyUserId,
        String replyUserName
) {
}
