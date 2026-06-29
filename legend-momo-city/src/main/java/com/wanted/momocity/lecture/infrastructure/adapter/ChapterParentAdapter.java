package com.wanted.momocity.lecture.infrastructure.adapter;

import com.wanted.momocity.lecture.domain.exception.ChapterNotFoundException;
import com.wanted.momocity.lecture.domain.repository.ChapterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// ChapterParentPort를 위한 Adapter
@Component
@RequiredArgsConstructor
public class ChapterParentAdapter implements  ChapterParentPort{

    private final ChapterRepository chapterRepository;

    @Override
    public Long getLectureIdByChapterId(Long chapterId) {

        return chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ChapterNotFoundException("챕터를 찾을 수 없습니다."))
                .getLectureId();
    }
}
