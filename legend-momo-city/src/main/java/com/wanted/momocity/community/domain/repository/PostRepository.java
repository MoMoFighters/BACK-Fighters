package com.wanted.momocity.community.domain.repository;

import com.wanted.momocity.community.application.result.PostWithContents;
import com.wanted.momocity.community.domain.model.Post;
import com.wanted.momocity.community.infrastructure.persistence.PostJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
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

    // 유저별 게시글 목록 커서 기반 조회
    // cursor = null -> 첫 페이지, cursor != null -> 해당 postId 보다 작은 데이터 조회
    List<Post> findByUserIdWithCursor(Long userId, Long cursor, int size);

    // 유저별 게시글 수 조회
    int countByUserId(Long userId);

    // 대시보드용 통계 조회 (총 조회수, 총 좋아요수)
    int sumViewCountByUserId(Long userId);
    int sumLikeCountByUserId(Long userId);

    // 키워드 검색 (커서 기반)
    List<Post> searchByKeyword(String keyword, Long cursor, int size);

    // 키워드 검색 결과 총 개수
    int countByKeyword(String keyword);

    // 같은 카테고리 인기 게시글 조회
    List<Post> findTopPostsByCategory(String category, Long postId, int size);

    // 같은 작성자 최신 게시글 조회
    List<Post> findLatestPostsByAuthor(Long userId, Long postId, List<Long> excludeIds, int size);

    // 게시글 하드딜리트
    int hardDeleteByDeletedAtBefore(LocalDateTime threshold);

}
