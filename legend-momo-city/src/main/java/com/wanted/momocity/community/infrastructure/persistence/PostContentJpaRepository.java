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
*  -
*  post_content 는 소프트딜리트 없이 하드딜리트만 사용
*  -> 컨텐츠 수정(PUT) 시 기존 전페 삭제 후 새로 저장
*  -> 게시글 삭제 시 연관 컨텐츠 즉시 삭제
*  -> 스케줄러 불필요 (즉시 물리 삭제)
* */

public interface PostContentJpaRepository extends JpaRepository<PostContentJpaEntity, Long> {

    // 게시글 콘텐츠 전체 조회 (소프트딜리트 제외, orderNo 기준 정렬)
    List<PostContentJpaEntity> findAllByPostIdOrderByOrderNoAsc(Long postId);

    // 하드딜리트
    // 게시글 콘텐츠 전체 물리 삭제
    // -> 콘텐츠 수정(PUT), 게시글 삭제(DELETE) 시 호출
    void deleteAllByPostId(Long postId);

}
