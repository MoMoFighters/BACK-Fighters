package com.wanted.momocity.community.presentation.api.response;

/*
* comment.
*  게시글 콘텐츠 단건 응답 DTO
*  - type : TEXT or IMAGE
*  - content : 텍스트 내용 (TEXT 타입)
*  - imageUrl : S3 URL (IMAGE 타입)
* */

public record PostContentResponse(
        String type,
        String content,
        String imageUrl
) {
}
