package com.wanted.momocity.community.infrastructure.persistence;

import com.wanted.momocity.community.domain.model.PostLike;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "post_like",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"post_id", "user_id"})
        })
@NoArgsConstructor
public class PostLikeJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    private void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    // Domain → JpaEntity 변환 (저장용)
    public static PostLikeJpaEntity from(PostLike domain) {
        PostLikeJpaEntity entity = new PostLikeJpaEntity();
        entity.id = domain.getId();
        entity.postId = domain.getPostId();
        entity.userId = domain.getUserId();
        return entity;
    }

    // JpaEntity → Domain 변환 (조회용)
    public PostLike toDomain() {
        return PostLike.reconstitute(id, postId, userId, createdAt);
    }

}
