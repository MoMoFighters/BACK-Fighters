package com.wanted.momocity.community.infrastructure.adapter;

import com.wanted.momocity.community.domain.model.PostLike;
import com.wanted.momocity.community.domain.repository.PostLikeRepository;
import com.wanted.momocity.community.infrastructure.persistence.PostLikeJpaEntity;
import com.wanted.momocity.community.infrastructure.persistence.PostLikeJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/*
* comment.
*  PostLikeRepository 인터페이스 구현체
*  -> domain.repository 인터페이스 <- 구현 -> JpaRepository 연결
* */

@Component
@RequiredArgsConstructor
public class PostLikeRepositoryAdapter implements PostLikeRepository {

    private final PostLikeJpaRepository postLikeJpaRepository;

    @Override
    public PostLike save(PostLike postLike) {
        return postLikeJpaRepository.save(PostLikeJpaEntity.from(postLike)).toDomain();
    }

    @Override
    public Optional<PostLike> findByPostIdAndUserId(Long postId, Long userId) {
        return postLikeJpaRepository.findByPostIdAndUserId(postId, userId)
                .map(PostLikeJpaEntity::toDomain);
    }

    @Override
    public List<PostLike> findAllByPostId(Long postId) {
        return postLikeJpaRepository.findAllByPostId(postId)
                .stream()
                .map(PostLikeJpaEntity::toDomain)
                .toList();
    }

    // 좋아요 존재 여부 확인 (boolean 만 반환)
    @Override
    public boolean existsByPostIdAndUserId(Long postId, Long userId) {
        return postLikeJpaRepository.existsByPostIdAndUserId(postId, userId);
    }

    @Override
    public void delete(PostLike postLike) {
        postLikeJpaRepository.delete(PostLikeJpaEntity.from(postLike));
    }
}
