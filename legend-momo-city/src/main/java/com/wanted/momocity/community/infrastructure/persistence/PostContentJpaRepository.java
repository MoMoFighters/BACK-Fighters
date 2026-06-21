package com.wanted.momocity.community.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/*
* comment.
*  Spring Data JPA 가 구현체를 자동으로 생성
*  -> Domain 을 모르고 JpaEntity 만 다룸
* */

public interface PostContentJpaRepository extends JpaRepository<PostContentJpaEntity, Long> {

    // 게시글 콘텐츠 전체 조회 (소프트딜리트 제외, orderNo 기준 정렬)
    List<PostContentJpaEntity> findAllByPostIdOrderByOrderNoAsc(Long postId);

    // 하드딜리트
    void deleteAllByPostId(Long postId);

}
