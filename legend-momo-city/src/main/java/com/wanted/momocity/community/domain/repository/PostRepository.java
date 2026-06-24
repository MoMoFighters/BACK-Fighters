package com.wanted.momocity.community.domain.repository;

import com.wanted.momocity.community.application.result.PostWithContents;
import com.wanted.momocity.community.domain.model.Post;
import com.wanted.momocity.community.infrastructure.persistence.PostJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Optional;

/*
* comment.
*  Post 도메인 저장소 인터페이스
*  - infrastructure 를 모르고 도메인 계층에서만 사용
*  - 구현체 : PostRepositoryAdapter
* */

public interface PostRepository {

    // 게시글 저장 (생성, 수정)
    Post save (Post post);

    // 게시글 단건 조회 (소프트딜리트 제외)
    Optional<Post> findById(Long postId);

    // 게시글 단건 조회 + contents fetch join (단건 조회 API 용)
    // Post + List<PostContent> 묶어서 반환
    Optional<PostWithContents> findByIdWithContents(Long postId);

    // 게시글 목록 조회 (소프트딜리트 제외, 페이지네이션)
    Page<Post> findAll(String category, Pageable pageable);

    // 게시글 하드딜리트
    int hardDeleteByDeletedAtBefore(LocalDateTime threshold);

}
