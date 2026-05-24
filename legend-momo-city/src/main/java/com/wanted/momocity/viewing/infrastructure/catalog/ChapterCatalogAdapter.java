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
       throw new UnsupportedOperationException("구현 예정");
    }

    @Override
    public List<Chapter> findAllByLectureId(Long lectureId) {
        throw new UnsupportedOperationException("구현 예정");
    }
}
