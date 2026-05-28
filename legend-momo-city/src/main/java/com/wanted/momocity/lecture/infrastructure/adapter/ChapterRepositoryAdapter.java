package com.wanted.momocity.lecture.infrastructure.adapter;

import com.wanted.momocity.lecture.domain.model.LectureAggregate;
import com.wanted.momocity.lecture.domain.model.LectureChapter;
import com.wanted.momocity.lecture.domain.repository.ChapterRepository;
import com.wanted.momocity.lecture.infrastructure.persistence.ChapterJpaEntity;
import com.wanted.momocity.lecture.infrastructure.persistence.SpringDataChapterRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// ChapterRepositoryAdapter는 도메인 Repository를 JPA Repository와 연결합니다.
@Repository
public class ChapterRepositoryAdapter implements ChapterRepository {

    private final SpringDataChapterRepository repository;

    public ChapterRepositoryAdapter(SpringDataChapterRepository repository) {
        this.repository = repository;
    }

    @Override
    public LectureChapter save(LectureChapter chapter) {
        ChapterJpaEntity entity = ChapterJpaEntity.from(chapter);

        ChapterJpaEntity saved = repository.save(entity);

        return saved.toDomain();
    }

    @Override
    public int countByLectureId(Long lectureId) {
        return repository.countByLectureId(lectureId);
    }

    @Override
    public boolean existsByLectureIdAndOrderNo(Long lectureId, int orderNo) {
        return repository.existsByLectureIdAndOrderNo(lectureId, orderNo);
    }

    @Override
    public Optional<LectureChapter> findById(Long chapterId) {
        return repository.findById(chapterId)
                .map(ChapterJpaEntity::toDomain);
    }

    @Override
    public boolean existsByLectureIdAndVideoUrlIsNull(Long lectureId) {
        return repository.existsByLectureIdAndVideoUrlIsNull(lectureId);
    }
    
}