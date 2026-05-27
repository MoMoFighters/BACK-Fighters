package com.wanted.momocity.lecture.infrastructure.persistence;

import com.wanted.momocity.lecture.domain.model.Lecture;
import com.wanted.momocity.lecture.domain.model.LectureCategory;
import com.wanted.momocity.lecture.domain.model.LecturePage;
import com.wanted.momocity.lecture.domain.repository.LectureRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class LectureRepositoryAdapter implements LectureRepository {

    private final SpringDataLectureRepository repository;

    public LectureRepositoryAdapter(SpringDataLectureRepository repository) {
        this.repository = repository;
    }

    @Override
    public Lecture save(Lecture lecture) {
        LectureJpaEntity entity = new LectureJpaEntity(
                lecture.getTeacherId(),
                lecture.getTitle(),
                lecture.getDescription(),
                lecture.getThumbnailUrl(),
                lecture.getCategory(),
                lecture.getStatus()
        );

        LectureJpaEntity saved = repository.save(entity);

        return toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Lecture> findById(Long lectureId) {
        return repository.findById(lectureId)
                .map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public LecturePage findLectures(
            LectureCategory category,
            Boolean enrolled,
            List<Long> enrolledLectureIds,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page - 1, size);

        Page<LectureJpaEntity> lecturePage = findLecturePage(
                category,
                enrolled,
                enrolledLectureIds,
                pageable
        );

        List<Lecture> content = lecturePage.getContent().stream()
                .map(this::toDomain)
                .toList();

        return new LecturePage(
                content,
                lecturePage.getTotalElements(),
                lecturePage.getTotalPages()
        );
    }

    private Page<LectureJpaEntity> findLecturePage(
            LectureCategory category,
            Boolean enrolled,
            List<Long> enrolledLectureIds,
            Pageable pageable
    ) {
        // enrolled 조건이 없으면 category 조건만 적용합니다.
        if (enrolled == null) {
            if (category == null) {
                return repository.findAll(pageable);
            }

            return repository.findAllByCategory(category, pageable);
        }

        // enrolled=true인데 신청한 강의가 없다면 결과는 빈 페이지
        if (Boolean.TRUE.equals(enrolled) && enrolledLectureIds.isEmpty()) {
            return Page.empty(pageable);
        }

        // enrolled=true이면 신청한 강의만 조회
        if (Boolean.TRUE.equals(enrolled)) {
            if (category == null) {
                return repository.findAllByIdIn(enrolledLectureIds, pageable);
            }

            return repository.findAllByCategoryAndIdIn(
                    category,
                    enrolledLectureIds,
                    pageable
            );
        }

        // enrolled=false인데 신청한 강의가 없다면 전체 강의를 조회
        if (enrolledLectureIds.isEmpty()) {
            if (category == null) {
                return repository.findAll(pageable);
            }

            return repository.findAllByCategory(category, pageable);
        }

        // enrolled=false이면 신청하지 않은 강의만 조회
        if (category == null) {
            return repository.findAllByIdNotIn(enrolledLectureIds, pageable);
        }

        return repository.findAllByCategoryAndIdNotIn(
                category,
                enrolledLectureIds,
                pageable
        );
    }

    private Lecture toDomain(LectureJpaEntity entity) {
        return Lecture.restore(
                entity.getId(),
                entity.getTeacherId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getThumbnailUrl(),
                entity.getCategory(),
                entity.getStatus(),
                entity.getCompletedUserCount(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}