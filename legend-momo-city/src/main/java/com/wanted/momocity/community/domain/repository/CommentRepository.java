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

    /*
    * comment.
    *  커서 기반 댓글 목록 조회
    *  cursor : 마지막을 조회한 commentId
    *  -> cursor = null -> 첫 페이지, != null -> 해당 commendId 이후 데이터 조회
    *  -
    *  최상위 댓글 (ParentId = NULL) + 대댓글 (ParentId != NULL) 포함 -> fetchJoin 으로 한 번에 조회
    * */
    List<Comment> findByPostIdWithCursor(Long postId, Long cursor, int size);

    /*
     * comment,
     *  커서 기반 대댓글 조회
     *  cursor = null -> 첫 페이지(5개), != null -> 해당 replyId 이후 데이터 조회
     * */
    List<Comment> findRepliesByCommentIdWithCursor(Long commentId, Long cursor, int size);


    // 게시글 최상위 댓글 수 조회 (대댓글 제외) -> PostCommentResponse 의 totalCount 에 사용
    int countByPostId(Long postId);

    // 특정 댓글의 댓글 수 조회 (getReplies totalCount 용)
    int countRepliesByCommentId(Long commentId);

    // 댓글 소프트딜리트
    void delete(Comment comment);

    // 댓글 하드딜리트
    int hardDeleteByDeletedAtBefore(LocalDateTime threshold);

}
