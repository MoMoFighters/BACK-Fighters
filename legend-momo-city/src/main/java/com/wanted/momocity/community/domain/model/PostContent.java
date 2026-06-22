package com.wanted.momocity.community.domain.model;

import java.time.LocalDateTime;

/*
* comment.
*  게시글 콘텐츠 도메인 모델
*  - TEXT 타입 : content 에 텓스트 저장
*  - IMAGE 타입 : imageUrl 에 S3 URL 저장
*  -
*  소프트 딜리트
*  - deletedAt == null : 정상
*  - deletedAt != null : 삭제된 게시물
* */

public class PostContent {

    public enum Type {
        TEXT, IMAGE
    }

    private Long id;
    private Long postId;
    private int orderNo;
    private Type type;
    private String content;
    private String imageUrl;
    private LocalDateTime createdAt;

    // 신규 생성용
    public static PostContent create(
            Long postId, int orderNo, Type type,
            String content, String imageUrl
    ) {
        PostContent postContent = new PostContent();
        postContent.postId = postId;
        postContent.orderNo = orderNo;
        postContent.type = type;
        postContent.content = content;
        postContent.imageUrl = imageUrl;
        return postContent;
    }

    // DB 복원용
    public static PostContent reconstitute(
            Long id, Long postId, int orderNo, Type type,
            String content, String imageUrl,
            LocalDateTime createdAt
    ) {
        PostContent postContent = new PostContent();
        postContent.id = id;
        postContent.postId = postId;
        postContent.orderNo = orderNo;
        postContent.type = type;
        postContent.content = content;
        postContent.imageUrl = imageUrl;
        postContent.createdAt = createdAt;
        return postContent;
    }

    public Long getId() { return id; }
    public Long getPostId() { return postId; }
    public int getOrderNo() { return orderNo; }
    public Type getType() { return type; }
    public String getContent() { return content; }
    public String getImageUrl() { return imageUrl; }
    public LocalDateTime getCreatedAt() { return createdAt; }

}
