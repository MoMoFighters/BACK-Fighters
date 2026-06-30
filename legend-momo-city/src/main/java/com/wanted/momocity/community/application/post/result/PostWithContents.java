package com.wanted.momocity.community.application.post.result;

import com.wanted.momocity.community.domain.model.Post;
import com.wanted.momocity.community.domain.model.PostContent;

import java.util.List;

/*
* comment.
*  게시글 단건 조회 시 Post + contents 묶음 객체
*  -
*  Post 도메인 모델은 순수 비즈니스 로직만 담당
*  -> contents 까지 담으면 도메인 모델이 무거워짐
*  -> application 레이어에서 조합해서 사용
*  -
*  PostQueryService.getPost() 에서 사용
*  -> post 정보 + contents 한 번에 조회
* */

public record PostWithContents(
        Post post,
        List<PostContent> contents
) {
}
