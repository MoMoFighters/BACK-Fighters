package com.wanted.momocity.community.infrastructure.persistence;

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

    // 하드딜리트 (스케줄러용)
    // deletedAt IS NOT NULL : 소프트딜리트된 댓글만 대상
    // deletedAt < threshold : 기준일(6개월) 이전 데이터만 삭제
    @Modifying
    @Query("DELETE FROM CommentJpaEntity c WHERE c.deletedAt IS NOT NULL AND c.deletedAt < :threshold")
    int hardDeleteByDeletedAtBefore(@Param("threshold") LocalDateTime threshold);

}
