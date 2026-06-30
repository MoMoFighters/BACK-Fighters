package com.wanted.momocity.community.application.like.usecase;

import com.wanted.momocity.community.presentation.api.response.PostLikeListResponse;

/*
* comment.
*  좋아요 읽기 작업 UseCase 인터페이스
*  -> 좋아요 목록 조스
* */

public interface LikeQueryUseCase {

    // 좋아요 누른 사용자 목록 조회
    PostLikeListResponse getLikes(Long postId);

}
