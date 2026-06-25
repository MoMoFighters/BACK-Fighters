package com.wanted.momocity.community.domain.repository;

import com.wanted.momocity.community.domain.model.PostLike;

import java.util.List;
import java.util.Optional;

/*
* comment.
*  PostLike 도메인 저장소 인터페이스
*  - 구현체 : PostLikeRepositoryAdapter
* */

public interface PostLikeRepository {

    // 좋아요 저장
    PostLike save(PostLike postLike);

    // 좋아요 여부 확인
    Optional<PostLike> findByPostIdAndUserId(Long postId, Long userId);

    List<PostLike> findAllByPostId(Long postId);

    // 좋아요 삭제
    void delete(PostLike postLike);

}
