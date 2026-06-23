package com.wanted.momocity.community.domain.repository;

import com.wanted.momocity.community.domain.model.Comment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/*
* Comment.
*  Comment 도메인 저장소 인터페이스
*   - infrastructure 를 모르고 도메인 계층에서만 사용
*   - 구현체 : CommentRepositoryAdapter
* */

public interface CommentRepository {

    // 댓글 / 대댓글 저장
    Comment save(Comment comment);

    // 댓글 단건 조회 (소프트딜리트 제외)
    Optional<Comment> findById(Long commentId);

    // 게시글 댓글 목록 조회 (소프트딜리트 제외)
    List<Comment> findAllByPostId(Long postId);

    // 게시글 목록의 댓글 수 일괄 조회 (N+1 개선)
    // postId 목록으로 GROUP BY 쿼리 -> MAP<postId, count> 로 반환
    Map<Long, Long> countByPostIds(List<Long> postIds);

    // 댓글 소프트딜리트
    void delete(Comment comment);

    // 댓글 하드딜리트
    int hardDeleteByDeletedAtBefore(LocalDateTime threshold);

}
