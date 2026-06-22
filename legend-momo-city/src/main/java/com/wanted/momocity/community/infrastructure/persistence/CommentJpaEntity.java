package com.wanted.momocity.community.infrastructure.persistence;

import com.wanted.momocity.community.domain.model.Comment;
import com.wanted.momocity.global.infrastructure.persistence.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/*
* comment.
*  DB  테이블과 1:1 매핑되는 JPA 클래스
*  -> Domain Model (Comment) 을 모르고 DB 컬럼 구조만 표현
*  -> 변환은 CommentRepositoryAdapter 가 담당
*  -> update_at 은 DB 레벨에서만 관리 (수정기능 없음)
* */

@Getter
@Entity
@Table(name = "comment")
@NoArgsConstructor
public class CommentJpaEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // Domain → JpaEntity 변환 (저장용)
    public static CommentJpaEntity from(Comment domain) {
        CommentJpaEntity entity = new CommentJpaEntity();
        entity.id = domain.getId();
        entity.postId = domain.getPostId();
        entity.userId = domain.getUserId();
        entity.parentId = domain.getParentId();
        entity.content = domain.getContent();
        entity.deletedAt = domain.getDeletedAt();
        return entity;
    }

    // JpaEntity → Domain 변환 (조회용)
    public Comment toDomain() {
        return Comment.reconstitute(
                id, postId, userId, parentId, content,
                getCreatedAt(), deletedAt
        );
    }

}
