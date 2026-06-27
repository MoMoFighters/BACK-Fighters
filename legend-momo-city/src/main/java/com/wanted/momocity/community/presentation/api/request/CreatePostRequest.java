package com.wanted.momocity.community.presentation.api.request;

import com.wanted.momocity.community.domain.model.PostCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/*
* comment.
*  게시글 생성 요청 DTO
*  - 제목, 카테고리, 썸네일만 받음
*  - 콘텐츠는 별도 API 로 업로드
* */

public record CreatePostRequest(
        @NotBlank(message = "제목을 입력해주세요.")
        @Size(max = 100, message = "제목은 100자 이하로 입력해주세요.")
        String title,

        @NotNull(message = "카테고리를 입력해주세요.")
        PostCategory category,

        String thumbnailUrl
) {
}
