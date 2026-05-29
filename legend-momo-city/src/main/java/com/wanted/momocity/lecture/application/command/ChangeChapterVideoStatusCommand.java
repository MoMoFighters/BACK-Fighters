package com.wanted.momocity.lecture.application.command;

import com.wanted.momocity.lecture.domain.model.VideoStatus;

// 챕터 동영상 상태 변경에 필요한 값을 담는 Command
public record ChangeChapterVideoStatusCommand(
        Long teacherId,
        Long lectureId,
        Long chapterId,
        VideoStatus videoStatus
) {
}