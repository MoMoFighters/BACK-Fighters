package com.wanted.momocity.viewing.infrastructure.catalog;

import com.wanted.momocity.viewing.application.port.ChapterPort;
import com.wanted.momocity.viewing.domain.model.Chapter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/*
* comment.
*  catalog 컨텍스트 소유의 Chapter 를 READ 전용으로 조회
*  ChapterPort 인터페이스 구현체
*  -
*  현재 : ChapterJpaRepository, ChapterJpaEntity 완성 후 주입 예정
* */

@Component
@RequiredArgsConstructor
public class ChapterCatalogAdapter implements ChapterPort {

    // ChapterJpaRepository 완성 후 주입 예정
    // private final ChapterJpaRepository chapterJpaRepository;

    @Override
    public Chapter findById(Long chapterId) {
        return Chapter.reconstitute(
                chapterId,
                1L,
                "임시 챕터 " + chapterId,
                1,
                "video/lecture1/chapter" + chapterId + ".mp4",
                600,
                Chapter.VideoStatus.READY
        );
    }

    @Override
    public List<Chapter> findAllByLectureId(Long lectureId) {
        return List.of(
                Chapter.reconstitute(1L, lectureId, "1강 임시 챕터", 1,
                        "video/lecture1/chapter1.mp4", 600, Chapter.VideoStatus.READY),
                Chapter.reconstitute(2L, lectureId, "2강 임시 챕터", 2,
                        "video/lecture1/chapter2.mp4", 600, Chapter.VideoStatus.READY),
                Chapter.reconstitute(3L, lectureId, "3강 임시 챕터", 3,
                        "video/lecture1/chapter3.mp4", 600, Chapter.VideoStatus.READY)
        );
    }
}
