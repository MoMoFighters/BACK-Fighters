package com.wanted.momocity.community.infrastructure.adapter;

import com.wanted.momocity.community.application.result.PostWithContents;
import com.wanted.momocity.community.domain.model.Post;
import com.wanted.momocity.community.domain.repository.PostRepository;
import com.wanted.momocity.community.infrastructure.persistence.PostContentJpaEntity;
import com.wanted.momocity.community.infrastructure.persistence.PostJpaEntity;
import com.wanted.momocity.community.infrastructure.persistence.PostJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/*
* comment.
*  PostRepository 인터페이스 구현체
*  -> domain.repository 인터페이스 <- 구현 -> JpaRepository 연결
*  - 저장 : Domain -> JpaEntity (from()) -> DB 저장
*  - 조회 : DB 조회 -> JpaEntity -> Domain (toDomain())
* */

@Component
@RequiredArgsConstructor
public class PostRepositoryAdapter implements PostRepository {

    private final PostJpaRepository postJpaRepository;

    @Override
    public Post save(Post post) {
        return postJpaRepository.save(PostJpaEntity.from(post)).toDomain();
    }

    @Override
    public Optional<Post> findById(Long postId) {
        return postJpaRepository.findByIdAndDeletedAtIsNull(postId)
                .map(PostJpaEntity::toDomain);
    }

    @Override
    public Page<Post> findAll(String category, Pageable pageable) {
        return postJpaRepository.findAllByCategory(category, pageable)
                .map(PostJpaEntity:: toDomain);
    }

    @Override
    @Transactional
    public int hardDeleteByDeletedAtBefore(LocalDateTime threshold) {
        return postJpaRepository.hardDeleteByDeletedAtBefore(threshold);
    }

    /*
    * comment.
    *  게시글 단건 조회 + contents fetch join
    *  -
    *  단건 조회 시 contents 항상 필요
    *  -> LAZY 로딩 시 N+1 발생 가능 -> fetch join 으로 한 번에 조회
    *  -
    *  PostJpaEntity -> Post 도메인 변환 -> PostContentJpaEntity
    *  -> PostContent 도메인 변환 -> PostWithContents 로 묶어서 반환
    * */

    @Override
    public Optional<PostWithContents> findByIdWithContents(Long postId) {
        return postJpaRepository.findByIdWithContents(postId)
                .map(entity -> new PostWithContents(
                        entity.toDomain(),
                        entity.getContents().stream()
                                .map(PostContentJpaEntity::toDomain)
                                .toList()
                ));
    }

    // 유저별 게시글 커서 기반 조회
    @Override
    public List<Post> findByUserIdWithCursor(Long userId, Long cursor, int size) {
        return postJpaRepository.findByUserIdWithCursor(
                        userId, cursor, PageRequest.of(0, size)
                )
                .stream()
                .map(PostJpaEntity::toDomain)
                .toList();
    }

    @Override
    public int countByUserId(Long userId) {
        return postJpaRepository.countByUserId(userId);
    }

    @Override
    public int sumViewCountByUserId(Long userId) {
        return postJpaRepository.sumViewCountByUserId(userId);
    }

    @Override
    public int sumLikeCountByUserId(Long userId) {
        return postJpaRepository.sumLikeCountByUserId(userId);
    }

    @Override
    public List<Post> searchByKeyword(String keyword, Long cursor, int size) {
        return postJpaRepository.searchByKeyword(keyword, cursor, PageRequest.of(0, size))
                .stream()
                .map(PostJpaEntity::toDomain)
                .toList();
    }

    @Override
    public int countByKeyword(String keyword) {
        return postJpaRepository.countByKeyword(keyword);
    }

    // 같은 카테고리 인기 게시글 조회
    // PageRequest.of(0, size) 로 상위 N개만 조회
    @Override
    public List<Post> findTopPostsByCategory(String category, Long postId, int size) {
        return postJpaRepository.findTopPostsByCategory(category, postId, PageRequest.of(0, size))
                .stream()
                .map(PostJpaEntity::toDomain)
                .toList();
    }

    // 같은 작성자의 최신 게시글 조회
    // excludeIds 비어있으면 빈 리스트 처리
    @Override
    public List<Post> findLatestPostsByAuthor(Long userId, Long postId, List<Long> excludeIds, int size) {
        List<Long> safeExcludeIds = excludeIds.isEmpty() ? List.of(-1L) : excludeIds;
        return postJpaRepository.findLatestPostsByAuthor(userId, postId, safeExcludeIds, PageRequest.of(0, size))
                .stream()
                .map(PostJpaEntity::toDomain)
                .toList();
    }
}