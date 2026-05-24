package com.wanted.momocity.viewing.application.command;

/*
* comment.
*  S3 Presigned URL 발급에 필요한 값 묶음
*  userId(토큰) + lectureId(PathVariable) + chapterId(PathVariable)
* */

public record GetStreamingUrlCommand(
        Long userId,
        Long lectureId,
        Long chapterId
) {
}
