package com.wanted.momocity.community.infrastructure.adapter;

import com.wanted.momocity.community.domain.model.Comment;
import com.wanted.momocity.community.domain.repository.CommentRepository;
import com.wanted.momocity.community.infrastructure.persistence.CommentJpaEntity;
import com.wanted.momocity.community.infrastructure.persistence.CommentJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/*
* comment.
*  CommentRepository 인터페이스 구현체
*  -> domain.repository 인터페이스 <- 구현 -> JpaRepository 연결
* */

@Component
@RequiredArgsConstructor
public class CommentRepositoryAdapter implements CommentRepository {

    private final CommentJpaRepository commentJpaRepository;

    @Override
    public Comment save(Comment comment) {
        return commentJpaRepository.save(CommentJpaEntity.from(comment)).toDomain();
    }

    @Override
    public Optional<Comment> findById(Long commentId) {
        return commentJpaRepository.findByIdAndDeletedAtIsNull(commentId)
                .map(CommentJpaEntity::toDomain);
    }

    @Override
    public List<Comment> findAllByPostId(Long postId) {
        return commentJpaRepository.findAllByPostIdAndDeletedAtIsNull(postId)
                .stream()
                .map(CommentJpaEntity::toDomain)
                .toList();
    }

    // 댓글 목록의 댓글 수 일괄 조회
    @Override
    public Map<Long, Long> countByPostIds(List<Long> postIds) {
        return commentJpaRepository.countByPostIds(postIds)
                .stream()
                .collect(Collectors.toMap(
                        // postId
                        row -> (Long) row[0],
                        // count
                        row -> (Long) row[1]
                ));
    }

    // 커서 기반 댓글 목록 조회
    @Override
    public List<Comment> findByPostIdWithCursor(Long postId, Long cursor, int size) {
        return commentJpaRepository.findByPostIdWithCursor(
                        // size + 1 개 조회 -> 다음 페이지 존재 여부 확인용
                postId, cursor, PageRequest.of(0, size + 1)
        )
                .stream()
                .map(CommentJpaEntity::toDomain)
                .toList();
    }

    // 특정 댓글의 대댓글 수 조회
    // COUNT 쿼리로 DB 레벨에서 집계
    @Override
    public int countRepliesByCommentId(Long commentId) {
        return commentJpaRepository.countRepliesByCommentId(commentId);
    }

    @Override
    public int countByPostId(Long postId) {
        return commentJpaRepository.countByPostId(postId);
    }

    // 커서 기반 대댓글 조회 -> CommentJpaEntity -> Comment 도메인 변환
    @Override
    public List<Comment> findRepliesByCommentIdWithCursor(Long commentId, Long cursor, int size) {
        return commentJpaRepository.findRepliesByCommentIdWithCursor(
                        // size + 1 개 조회 -> 다음 페이지 존재 여부 확인용
                        commentId, cursor, PageRequest.of(0, size + 1)
                )
                .stream()
                .map(CommentJpaEntity::toDomain)
                .toList();
    }

    // 댓글 소프트딜리트
    // deleted_at 업데이트
    @Override
    public void delete(Comment comment) {
        commentJpaRepository.save(CommentJpaEntity.from(comment));
    }

    @Override
    @Transactional
    public int hardDeleteByDeletedAtBefore(LocalDateTime threshold) {
        return commentJpaRepository.hardDeleteByDeletedAtBefore(threshold);
    }

}
