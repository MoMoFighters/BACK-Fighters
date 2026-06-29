package com.wanted.momocity.community.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/*
* comment.
*  Spring Data JPA 가 구현체를 자동으로 생성
*  -> Domain 을 모르고 JpaEntity 만 다룸
* */

public interface PostLikeJpaRepository extends JpaRepository<PostLikeJpaEntity, Long> {

    // 좋아요 여부 확인
    Optional<PostLikeJpaEntity> findByPostIdAndUserId(Long postId, Long userId);

    // 좋아요 누른 사용자 목록 조회
    List<PostLikeJpaEntity> findAllByPostId(Long postId);

    /*
     * comment.
     *  좋아요 존재 여부 확인
     *  -
     *  findByPostIdAndUserId() 는 PostLike 전체 객체 로드
     *  -> isLiked 확인용으로 boolean 만 필요 -> EXISTS 쿼리로 불필요한 데이터 로드 방지
     */

    @Query("""
    SELECT COUNT(l) > 0
    FROM PostLikeJpaEntity l
    WHERE l.postId = :postId
    AND l.userId = :userId
""")
    boolean existsByPostIdAndUserId(
            @Param("postId") Long postId,
            @Param("userId") Long userId
    );


}
