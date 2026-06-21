package com.wanted.momocity.community.infrastructure.persistence;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

/*
* comment.
*  Spring Data JPA 가 구현체를 자동으로 생성
*  -> Domain 을 모르고 JpaEntity 만 다룸
* */

public interface PostJpaRepository extends JpaRepository<PostJpaEntity, Long> {

    // 단건 조회 (소프트딜리트 제외)
    Optional<PostJpaEntity> findByIdAndDeletedAtIsNull(Long PostId);

    // 목록 조회 (소프트딜리트 제외, 카테고리 필터링)
    @Query("""
        SELECT p FROM PostJpaEntity p
        WHERE p.deletedAt IS NULL
        AND (:category IS NULL OR p.category = :category)
        ORDER BY p.createdAt DESC
    """)
    Page<PostJpaEntity> findAllByCategory(
            @Param("category") String category,
            Pageable pageable
    );

    @Modifying
    @Query("DELETE FROM PostJpaEntity p WHERE p.deletedAt IS NOT NULL AND p.deletedAt < :threshold")
    int hardDeleteByDeletedAtBefore(@Param("threshold") LocalDateTime threshold);

}
