package com.wanted.momocity.lecture.infrastructure.adapter;

import com.wanted.momocity.lecture.domain.model.LectureAggregate;
import com.wanted.momocity.lecture.domain.model.LectureChapter;
import com.wanted.momocity.lecture.domain.model.VideoStatus;
import com.wanted.momocity.lecture.domain.repository.ChapterRepository;
import com.wanted.momocity.lecture.infrastructure.persistence.ChapterJpaEntity;
import com.wanted.momocity.lecture.infrastructure.persistence.SpringDataChapterRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

// ChapterRepositoryAdapter는 도메인 Repository를 JPA Repository와 연결
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

    // 특정 강의에 속한 챕터 목록을 orderNo 오름차순으로 조회
    @Override
    public List<LectureChapter> findAllByLectureIdOrderByOrderNoAsc(Long lectureId) {
        return repository.findAllByLectureIdOrderByOrderNoAsc(lectureId)
                .stream()
                .map(ChapterJpaEntity::toDomain)
                .toList();
    }

    // 특정 강의의 챕터 중 지정한 영상 상태가 아닌 챕터가 있는지 확인
    @Override
    @Transactional(readOnly = true)
    public boolean existsByLectureIdAndVideoStatusNot(
            Long lectureId,
            VideoStatus videoStatus
    ) {
        return repository.existsByLectureIdAndVideoStatusNot(
                lectureId,
                videoStatus
        );
    }
    
}