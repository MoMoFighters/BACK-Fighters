package com.wanted.momocity.lecture.infrastructure.persistence;

import com.wanted.momocity.lecture.domain.model.LectureAggregate;
import com.wanted.momocity.lecture.domain.model.LectureCategory;
import com.wanted.momocity.lecture.domain.model.LecturePage;
import com.wanted.momocity.lecture.domain.model.LectureStatus;
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

    // 강의를 저장합니다.
    @Override
    public LectureAggregate save(LectureAggregate lecture) {
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

    // 강의 ID로 강의를 조회합니다.
    @Override
    @Transactional(readOnly = true)
    public Optional<LectureAggregate> findById(Long lectureId) {
        return repository.findById(lectureId)
                .map(this::toDomain);
    }

    // 학생용 강의 목록을 조회합니다.
    // 학생용 목록은 기본적으로 ACTIVE 상태의 강의만 내려줍니다.
    @Override
    @Transactional(readOnly = true)
    public LecturePage findLectures(
            LectureCategory category,
            Boolean enrolled,
            List<Long> enrolledLectureIds,
            int page,
            int size
    ) {
        // 프론트는 page를 1부터 보내고, Spring Data JPA는 page를 0부터 시작합니다.
        Pageable pageable = PageRequest.of(page - 1, size);

        Page<LectureJpaEntity> lecturePage = findLecturePage(
                category,
                enrolled,
                enrolledLectureIds,
                pageable
        );

        List<LectureAggregate> content = lecturePage.getContent().stream()
                .map(this::toDomain)
                .toList();

        return new LecturePage(
                content,
                lecturePage.getTotalElements(),
                lecturePage.getTotalPages()
        );
    }

    // category, enrolled 조건에 맞춰 ACTIVE 강의만 조회합니다.
    private Page<LectureJpaEntity> findLecturePage(
            LectureCategory category,
            Boolean enrolled,
            List<Long> enrolledLectureIds,
            Pageable pageable
    ) {
        // 학생용 강의 목록은 항상 ACTIVE 강의만 조회합니다.
        LectureStatus status = LectureStatus.ACTIVE;

        // enrolled 조건이 없으면 수강 여부와 상관없이 ACTIVE 강의를 조회합니다.
        if (enrolled == null) {
            if (category == null) {
                return repository.findAllByStatus(status, pageable);
            }

            return repository.findAllByCategoryAndStatus(
                    category,
                    status,
                    pageable
            );
        }

        // enrolled=true인데 신청한 강의가 없다면 결과는 빈 페이지입니다.
        if (Boolean.TRUE.equals(enrolled) && enrolledLectureIds.isEmpty()) {
            return Page.empty(pageable);
        }

        // enrolled=true이면 신청한 ACTIVE 강의만 조회합니다.
        if (Boolean.TRUE.equals(enrolled)) {
            if (category == null) {
                return repository.findAllByStatusAndIdIn(
                        status,
                        enrolledLectureIds,
                        pageable
                );
            }

            return repository.findAllByCategoryAndStatusAndIdIn(
                    category,
                    status,
                    enrolledLectureIds,
                    pageable
            );
        }

        // enrolled=false인데 신청한 강의가 없다면 신청 제외할 강의가 없으므로 ACTIVE 강의 전체를 조회합니다.
        if (enrolledLectureIds.isEmpty()) {
            if (category == null) {
                return repository.findAllByStatus(status, pageable);
            }

            return repository.findAllByCategoryAndStatus(
                    category,
                    status,
                    pageable
            );
        }

        // enrolled=false이면 신청하지 않은 ACTIVE 강의만 조회합니다.
        if (category == null) {
            return repository.findAllByStatusAndIdNotIn(
                    status,
                    enrolledLectureIds,
                    pageable
            );
        }

        return repository.findAllByCategoryAndStatusAndIdNotIn(
                category,
                status,
                enrolledLectureIds,
                pageable
        );
    }

    // JPA Entity를 도메인 모델로 변환합니다.
    private LectureAggregate toDomain(LectureJpaEntity entity) {
        return LectureAggregate.restore(
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