package com.wanted.momocity.viewing.infrastructure.adapter;

import com.wanted.momocity.viewing.application.port.ChapterProgressInfo;
import com.wanted.momocity.viewing.application.port.LectureChapterProgressPort;
import com.wanted.momocity.viewing.application.service.ViewingQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/*
* comment.
*  LectureChapterProgressPort 구현체
*  ViewingQueryService.getChapterProgress() 에서 progressRate 만 추출
*  순환참조 방지를 위해 별도의 Adapter 로 분리
* */

@Component
@RequiredArgsConstructor
public class LectureChapterProgressAdapter implements LectureChapterProgressPort {

    private final ViewingQueryService viewingQueryService;

    @Override
    public List<ChapterProgressInfo> getLectureChapterProgress(Long userId, Long lectureId) {
        return viewingQueryService.getChapterProgress(userId, lectureId)
                .chapters()
                .stream()
                .map(item -> new ChapterProgressInfo(
                        item.chapterId(),
                        item.chapterProgress(),
                        item.isCompleted(),
                        item.isAccessible()
                ))
                .toList();
    }
}
