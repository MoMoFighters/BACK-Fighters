package com.wanted.momocity.community.domain.model;

import java.time.LocalDateTime;

/*
* comment.
*  댓글 / 대댓글 도메인 모델
*  - parentId == null : 댓글
*  - parentId != null : 대댓글
*  -
*  - 소프트 딜리트
*  - deletedAt == null : 정상
*  - deletedAt != null : 삭제된 게시물
*
* */

public class Comment {

    private Long id;
    private Long postId;
    private Long userId;
    private Long parentId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;

    // 신규 생성용 (댓글)
    public static Comment create(Long postId, Long userId, String content) {
        Comment comment = new Comment();
        comment.postId = postId;
        comment.userId = userId;
        comment.parentId = null;
        comment.content = content;
        return comment;
    }

    // 신규 생성용 (대댓글)
    public static Comment createReply(
            Long postId, Long userId, Long parentId, String content
    ) {
        Comment comment = new Comment();
        comment.postId = postId;
        comment.userId = userId;
        comment.parentId = parentId;
        comment.content = content;
        return comment;
    }

    // DB 복원용
    public static Comment reconstitute(
            Long id, Long postId, Long userId, Long parentId, String content,
            LocalDateTime createdAt, LocalDateTime deletedAt
    ) {
        Comment comment = new Comment();
        comment.id = id;
        comment.postId = postId;
        comment.userId = userId;
        comment.parentId = parentId;
        comment.content = content;
        comment.createdAt = createdAt;
        comment.deletedAt = deletedAt;
        return comment;
    }

    // 소프트딜리트
    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }

    // 삭제 여부 확인
    public boolean isDeleted() { return this.deletedAt != null; }

    // 대댓글 여부 확인
    public boolean isReply() { return this.parentId != null; }

    public Long getId() { return id; }
    public Long getPostId() { return postId; }
    public Long getUserId() { return userId; }
    public Long getParentId() { return parentId; }
    public String getContent() { return content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getDeletedAt() { return deletedAt; }
}
