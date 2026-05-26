package com.wanted.momocity.lecture.infrastructure.persistence;

import com.wanted.momocity.lecture.domain.model.Lecture;

import com.wanted.momocity.lecture.domain.repository.LectureRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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

    /*
     * JPA Entity를 순수 도메인 모델로 변환한다.
     *
     * Controller나 Application 계층으로 JPA Entity를 직접 넘기지 않기 위해 사용한다.
     */
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