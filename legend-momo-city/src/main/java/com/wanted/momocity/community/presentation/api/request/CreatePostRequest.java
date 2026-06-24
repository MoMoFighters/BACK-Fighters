package com.wanted.momocity.community.presentation.api.request;

import jakarta.validation.constraints.NotBlank;

/*
* comment.
*  게시글 생성 요청 DTO
*  - 제목, 카테고리만 받음
*  - 콘텐츠는 별도 API 로 업로드
* */

public record CreatePostRequest(
        @NotBlank(message = "제목을 입력해주세요.")
        String title,

        @NotBlank(message = "카테고리를 입력해주세요.")
        String category
) {
}
