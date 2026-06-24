package com.wanted.momocity.community.infrastructure.adapter;

import com.wanted.momocity.community.application.result.PostWithContents;
import com.wanted.momocity.community.domain.model.Post;
import com.wanted.momocity.community.domain.repository.PostRepository;
import com.wanted.momocity.community.infrastructure.persistence.PostContentJpaEntity;
import com.wanted.momocity.community.infrastructure.persistence.PostJpaEntity;
import com.wanted.momocity.community.infrastructure.persistence.PostJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
}