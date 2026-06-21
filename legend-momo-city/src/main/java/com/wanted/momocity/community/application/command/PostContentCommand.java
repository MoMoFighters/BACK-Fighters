package com.wanted.momocity.community.application.command;

/*
* comment.
*  게시글 콘텐츠 업로드 / 수정 시 사용하는 커맨드 DTO
*  -> application 계층에서 사용
*  -> Controller 에서 Request -> Command 변환 후 UseCase 에 전달
*  -
*  TEXT 타입 -> content 에 텍스트, imageUrl = null
*  IMAGE 타입 -> imageUrl 에 S3 URL, content = null
* */

public record PostContentCommand(
//        int orderNo,
        String type,
        String content,
        String imageUrl
) {
}
