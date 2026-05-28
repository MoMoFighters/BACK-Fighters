package com.wanted.momocity.lecture.application.command;

import com.wanted.momocity.lecture.domain.model.LectureStatus;

// 강의 상태 변경에 필요한 값을 담는 Command
public record ChangeLectureStatusCommand(
        Long teacherId,
        Long lectureId,
        LectureStatus lectureStatus
) {
}