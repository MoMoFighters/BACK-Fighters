package com.wanted.momocity.community.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/*
* comment.
*  Spring Data JPA 가 구현체를 자동으로 생성
*  -> Domain 을 모르고 JpaEntity 만 다룸
* */

public interface PostLikeJpaRepository extends JpaRepository<PostLikeJpaEntity, Long> {

    // 좋아요 여부 확인
    Optional<PostLikeJpaEntity> findByPostIdAndUserId(Long postId, Long userId);

}
