package com.wanted.momocity.community.domain.model;

import java.time.LocalDateTime;

/*
* comment.
*  게시글 좋아요 도메인 모델
*  - (post_id, user_id) UNIQUE 제약으로 중복 좋아요 방지
* */

public class PostLike {

    private Long id;
    private Long postId;
    private Long userId;
    private LocalDateTime createdAt;

    // 신규 생성용
    public static PostLike create(Long postId, Long userId) {
        PostLike postLike = new PostLike();
        postLike.postId = postId;
        postLike.userId = userId;
        return postLike;
    }

    // DB 복원용
    public static PostLike reconstitute(
            Long id, Long postId, Long userId, LocalDateTime createdAt
    ) {
        PostLike postLike = new PostLike();
        postLike.id = id;
        postLike.postId = postId;
        postLike.userId = userId;
        postLike.createdAt = createdAt;
        return postLike;
    }

    public Long getId() { return id; }
    public Long getPostId() { return postId; }
    public Long getUserId() { return userId; }
    public LocalDateTime getCreatedAt() { return createdAt; }

}
