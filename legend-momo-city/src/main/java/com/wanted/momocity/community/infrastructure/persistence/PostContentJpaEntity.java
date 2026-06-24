package com.wanted.momocity.community.infrastructure.persistence;

import com.wanted.momocity.community.domain.model.PostContent;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/*
* comment.
*  DB 테이블과 1:1 매핑되는 JPA 클래스
*  -> Domain Model (PostContent) 을 모르고 DB 컬럼 구조만 표현
*  -> 변환은 PostContentRepositoryAdapter 가 담당
*  -
*  연간관계
*  @ManyToOne post -> PostJpaEntity 와 N:1 관계
*  insertable = false, update = false : post_id 컬럼은 이미 @Column 으로 관리
*  -> JPA 가 중복으로 컬럼 관리하지 않도록 방지
* */

@Getter
@Entity
@Table(name = "post_content")
@NoArgsConstructor
public class PostContentJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    // PostJpaEntity 와 N:1
    // post_id 컬럼은 위 @Column 으로 관리, 연관관계는 조회 전용
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", insertable = false, updatable = false)
    private PostJpaEntity post;

    @Column(name = "order_no", nullable = false)
    private int orderNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private PostContent.Type type;

    @Column(name = "content")
    private String content;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Domain → JpaEntity 변환 (저장용)
    public static PostContentJpaEntity from(PostContent domain) {
        PostContentJpaEntity entity = new PostContentJpaEntity();
        entity.id = domain.getId();
        entity.postId = domain.getPostId();
        entity.orderNo = domain.getOrderNo();
        entity.type = domain.getType();
        entity.content = domain.getContent();
        entity.imageUrl = domain.getImageUrl();
        return entity;
    }

    // JpaEntity → Domain 변환 (조회용)
    public PostContent toDomain() {
        return PostContent.reconstitute(
                id, postId, orderNo, type,
                content, imageUrl,
                createdAt
        );
    }

}
