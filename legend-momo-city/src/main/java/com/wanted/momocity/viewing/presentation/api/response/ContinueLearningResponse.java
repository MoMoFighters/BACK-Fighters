package com.wanted.momocity.viewing.presentation.api.response;

public record ContinueLearningResponse(
        Long lectureId,
        String lectureTitle,
        Long chapterId,
        String chapterTitle,
        int chapterProgress
) {
}
