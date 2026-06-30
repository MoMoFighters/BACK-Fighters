package com.wanted.momocity.community.application.like.usecase;

import com.wanted.momocity.community.application.like.result.LikeResult;

/*
* comment.
*  좋아요 쓰기 작업 UseCase 인터페이스
*  -> 좋아요, 좋아요 취소
* */

public interface LikeCommandUseCase {

    // 좋아요
    LikeResult likePost(Long userId, Long postId);

    // 좋아요 취소
    LikeResult unlikePost(Long userId, Long postId);


}
