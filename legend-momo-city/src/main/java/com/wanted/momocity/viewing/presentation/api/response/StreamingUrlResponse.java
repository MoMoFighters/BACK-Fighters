package com.wanted.momocity.viewing.presentation.api.response;

/*
* comment.
*  Service 처리 결과를 Controller 가 클라이언트에 돌려줄 형태로 포장, record 라서 불변 객체
*  -
*  S3 Presigned URL + 챕터 기본 정보를 프론트에 전달
* */

public record StreamingUrlResponse(
        Long chapterId,
        String presignedUrl,
        // URL 유효 시간 (초)
        int expiresIn,
        String videoTitle,
        int durationSec
) {
}
