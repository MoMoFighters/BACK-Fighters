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
        if (chapter.getId() != null) {
            // 전달받은 chapterId로 기존 JPA Entity를 조회합니다.
            ChapterJpaEntity entity = repository.findById(chapter.getId())
                    // 없으면 예외
                    .orElseThrow(() -> new IllegalArgumentException("챕터를 찾을 수 없습니다."));

            // 도메인 객체에서 변경된 챕터 정보를 기존 Entity에 반영
            entity.updateFromDomain(chapter);

            return entity.toDomain();
        }

        // 새 챕터를 등록하는 경우에는 도메인 객체를 JPA Entity로 변환
        ChapterJpaEntity entity = ChapterJpaEntity.from(chapter);

        // 새 챕터 Entity를 DB에 저장합니다.
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
    public boolean existsByLectureIdAndOrderNoAndIdNot(Long lectureId, int orderNo, Long chapterId) {
        return repository.existsByLectureIdAndOrderNoAndIdNot(
                lectureId,
                orderNo,
                chapterId
        );
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

    // 챕터 삭제
    @Override
    public void deleteById(Long chapterId) {
        repository.deleteById(chapterId);
    }

    // 강의 삭제 시 해당 강의의 챕터도 모두 삭제
    @Override
    public void deleteAllByLectureId(Long lectureId) {
        repository.deleteAllByLectureId(lectureId);
    }

}