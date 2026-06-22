package com.wanted.momocity.community.application.usecase;

import com.wanted.momocity.community.presentation.api.response.PostDetailResponse;
import com.wanted.momocity.community.presentation.api.response.PostListResponse;

public interface PostQueryUseCase {

    PostListResponse getPosts(Long userId, String category, int page, int size);

    PostDetailResponse getPost(Long userId, Long PostId);

}
