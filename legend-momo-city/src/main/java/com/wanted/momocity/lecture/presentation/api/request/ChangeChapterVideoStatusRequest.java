package com.wanted.momocity.lecture.presentation.api.request;

import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.momocity.lecture.application.command.ChangeChapterVideoStatusCommand;
import com.wanted.momocity.lecture.domain.model.VideoStatus;

// 챕터 동영상 상태 변경 요청 DTO
public record ChangeChapterVideoStatusRequest(
        String videoStatus
) {
    public ChangeChapterVideoStatusCommand toCommand(
            Long teacherId,
            Long lectureId,
            Long chapterId
    ) {
        return new ChangeChapterVideoStatusCommand(
                teacherId,
                lectureId,
                chapterId,
                parseVideoStatus(videoStatus)
        );
    }

    private VideoStatus parseVideoStatus(String videoStatus) {
        if (videoStatus == null || videoStatus.isBlank()) {
            throw new DomainRuleViolationException("동영상 상태는 필수입니다.");
        }

        try {
            return VideoStatus.valueOf(videoStatus.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new DomainRuleViolationException("허용되지 않은 동영상 상태입니다.");
        }
    }
}