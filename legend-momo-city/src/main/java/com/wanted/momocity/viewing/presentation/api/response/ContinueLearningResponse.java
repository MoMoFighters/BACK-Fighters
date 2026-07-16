package com.wanted.momocity.viewing.presentation.api.response;

public record ContinueLearningResponse(
        Long lectureId,
        String lectureTitle,
        Long chapterId,
        String chapterTitle,
        String chapterThumbnailUrl,
        int chapterProgress
) {
}
