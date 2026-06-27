package com.wanted.momocity.community.infrastructure.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/*
* comment.
*  Spring Data JPA 가 구현체를 자동으로 생성
*  -> Domain 을 모르고 JpaEntity 만 다룸
* */

public interface CommentJpaRepository extends JpaRepository<CommentJpaEntity, Long> {

    // 단건 조회 (소프트딜리트 제외)
    Optional<CommentJpaEntity> findByIdAndDeletedAtIsNull(Long commentId);

    // 게시글 전체 댓글 조회 (소프트딜리트 제외)
    List<CommentJpaEntity> findAllByPostIdAndDeletedAtIsNull(Long postId);

    /*
    * comment.
    *  게시글 목록의 댓글 수 일괄 조회
    *  -
    *  N+1 개선
    *  기존 : 게시글마다 findAllByPostId() 호출 -> N번 쿼리
    *  개선 : postId 목록으로 IN 쿼리 + GROUP BY -> 1번 쿼리
    *  -
    *  반환 형태
    *  Object[] -> [postId, count] 형태
    *  -> PostQueryService 에서 Map<Long, Long> 으로 변환
    * */

    @Query("""
    SELECT c.postId, COUNT(c)
    FROM CommentJpaEntity c
    WHERE c.postId IN :postIds
    AND c.deletedAt IS NULL
    GROUP BY c.postId
""")
    List<Object[]> countByPostIds(@Param("postIds") List<Long> postIds);

    /*
    * comment.
    *  커서 기반 댓글 목록 조회
    *  -
    *  cursor = null -> 첫 페이지 (id 기준 내림차순)
    *  cursor != null -> 해당 id 보다 작은 데이터 조회
    *  -
    *  replies fetch join -> 대댓글 한 번에 조회
    *  -
    *  parentId = NULL -> 최상위 댓글만 조회
    *  parentId != NULL -> replies fetch join 으로 포함
    * */

    @Query("""
        SELECT DISTINCT c FROM CommentJpaEntity c
        WHERE c.postId = :postId
        AND c.parentId IS NULL
        AND c.deletedAt IS NULL
        AND (:cursor IS NULL OR c.id < :cursor)
        ORDER BY c.id DESC
    """)
    List<CommentJpaEntity> findByPostIdWithCursor(
            @Param("postId") Long postId,
            @Param("cursor") Long cursor,
            org.springframework.data.domain.Pageable pageable
    );

    // 게시글 최상위 댓글 수 조회 (대댓글 기준)
    @Query("""
        SELECT COUNT(c)
        FROM CommentJpaEntity c
        WHERE c.postId = :postId
        AND c.parentId IS NULL
        AND c.deletedAt IS NULL
    """)
    int countByPostId(@Param("postId") Long postId);

    /*
     * comment.
     *  특정 댓글의 대댓글 수 조회
     *  -> getReplies() 의 totalCount 용
     *  -> Integer.MAX_VALUE 로 전체 조회 후 size() 로 카운트하는 방식 개선
     *  -> COUNT 쿼리로 DB 레벨에서 집계
     */
    @Query("""
    SELECT COUNT(c)
    FROM CommentJpaEntity c
    WHERE c.parentId = :commentId
    AND c.deletedAt IS NULL
""")
    int countRepliesByCommentId(@Param("commentId") Long commentId);

    /*
     * comment.
     *  커서 기반 대댓글 조회
     *  parentId = commentId -> 대댓글만 조회 (parentId != NULL)
     *  cursor = null -> 첫페이지, cursor != null -> 해당 id 보다 큰 데이터 조회
     *  -> 오래된 순서로 조회(ASC)
     * */

    @Query("""
    SELECT c FROM CommentJpaEntity c
    WHERE c.parentId = :commentId
    AND c.deletedAt IS NULL
    AND (:cursor IS NULL OR c.id > :cursor)
    ORDER BY c.id ASC
""")
    List<CommentJpaEntity> findRepliesByCommentIdWithCursor(
            @Param("commentId") Long commentId,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    // 하드딜리트 (스케줄러용)
    // deletedAt IS NOT NULL : 소프트딜리트된 댓글만 대상
    // deletedAt < threshold : 기준일(6개월) 이전 데이터만 삭제
    @Modifying
    @Query("DELETE FROM CommentJpaEntity c WHERE c.deletedAt IS NOT NULL AND c.deletedAt < :threshold")
    int hardDeleteByDeletedAtBefore(@Param("threshold") LocalDateTime threshold);

}
