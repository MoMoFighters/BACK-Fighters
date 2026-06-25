package com.wanted.momocity.community.infrastructure.persistence;

import com.wanted.momocity.community.domain.model.Post;
import com.wanted.momocity.global.infrastructure.persistence.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/*
* comment.
*  DB 테이블과 1:1 매핑되는 JPA 클래스
*  -> Domain Model (Post) 을 모르고 DB 컬럼 구조만 표현
*  -> 변환은 PostRepositoryAdapter 가 담당
*  -
*  연간관계
*  @OneToMany contents : 게시글 콘텐츠 목록
*  -
*  fetch 전략 : LAZY - 실제 사용 시점에 조회
*  -> 목록 조회 시 불필요한 contents  조회 방지
*  -> 단건 조회 시 fetch join 으로 한 번에 조회
* */

@Getter
@Entity
@Table(name = "post")
@NoArgsConstructor
public class PostJpaEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "view_count", nullable = false)
    private int viewCount;

    @Column(name = "post_like", nullable = false)
    private int likeCount;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // 게시글 콘텐츠 목록
    // mappedBy = "post" : PostContentJpaEntity.post 필드로 매핑, orderNo 기준 정렬
    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY)
    @OrderBy("orderNo ASC")
    private List<PostContentJpaEntity> contents = new ArrayList<>();

    // Domain -> JpaEntity 변환 (저장용)
    public static PostJpaEntity from(Post domain) {
        PostJpaEntity entity = new PostJpaEntity();
        entity.id = domain.getId();
        entity.userId = domain.getUserId();
        entity.title = domain.getTitle();
        entity.viewCount = domain.getViewCount();
        entity.likeCount = domain.getLikeCount();
        entity.category = domain.getCategory();
        entity.thumbnailUrl = domain.getThumbnailUrl();
        entity.deletedAt = domain.getDeletedAt();
        return entity;
    }

    // JpaEntity -> Domain 변환 (조회용)
    public Post toDomain() {
        return Post.reconstitute(
                id, userId, title, category,
                thumbnailUrl, viewCount, likeCount,
                getCreatedAt(), getUpdatedAt(), deletedAt
        );
    }

}
