package com.wanted.momocity.lecture.application.command;

import com.wanted.momocity.lecture.domain.model.LectureCategory;

// CreateLectureCommand는 강의 등록 유스케이스에 필요한 입력값 record
public record CreateLectureCommand(
        Long teacherId,
        String title,
        String description,
        String thumbnailUrl,
        LectureCategory category
) {
}