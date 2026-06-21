package com.wanted.momocity.community.domain.model;

import java.time.LocalDateTime;

/*
* comment.
*  게시글 도메인 역할 -> 순수 비지니스 규칙만 담당 (JPA 모름)
*  -
*  소프트 딜리트
*  - deletedAt == null : 정상
*  - deletedAt != null : 삭제된 게시물
* */

public class Post {

    private Long id;
    private Long userId;
    private String title;
    private String category;
    private String thumbnailUrl;
    private int viewCount;
    private int likeCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    // 신규 생성용
    public static Post create(Long userId, String title, String category) {
        Post post = new Post();
        post.userId = userId;
        post.title = title;
        post.category = category;
        post.thumbnailUrl = null;
        post.viewCount = 0;
        post.likeCount = 0;
        return post;
    }

    // DB 복원용
    public static Post reconstitute(
            Long id, Long userId, String title, String category,
            String thumbnailUrl, int viewCount, int likeCount,
            LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt
    ) {
        Post post = new Post();
        post.id = id;
        post.userId = userId;
        post.title = title;
        post.category = category;
        post.thumbnailUrl = thumbnailUrl;
        post.viewCount = viewCount;
        post.likeCount = likeCount;
        post.createdAt = createdAt;
        post.updatedAt = updatedAt;
        post.deletedAt = deletedAt;
        return post;
    }

    // 제목/카테고리 수정
    public void update(String title, String category) {
        this.title = title;
        this.category = category;
    }

    // 썸네일 수정
    // 컨텐츠 업로드 / 수정 시 호출
    public void updateThumbnail(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    // 소프트딜리트
    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }

    // 삭제 여부 확인
    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    // 조회수 증가
    public void increaseViewCount() {
        this.viewCount++;
    }

    // 좋아요 증가
    public void increaseLikeCount() {
        this.likeCount++;
    }

    // 좋아요 감소
    public void decreaseLikeCount() {
        if (this.likeCount > 0) this.likeCount--;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public int getViewCount() { return viewCount; }
    public int getLikeCount() { return likeCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getDeletedAt() { return deletedAt; }

}
