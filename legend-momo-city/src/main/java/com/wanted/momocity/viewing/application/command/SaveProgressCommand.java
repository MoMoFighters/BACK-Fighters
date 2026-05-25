package com.wanted.momocity.viewing.application.command;

/*
* comment.
*  진척도 저장에 필요한 값 묶음
*  usrId(토큰) + lectureId(PathVariable) + chapterId(PathVariable) + playbackSeconds(RequestBody)
* */

public record SaveProgressCommand(
        Long userId,
        Long lectureId,
        Long chapterId,
        int playbackSeconds
) {
}
