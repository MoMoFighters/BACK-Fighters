package com.wanted.momocity.lecture.infrastructure.persistence;

import com.wanted.momocity.lecture.domain.model.LectureCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

/**
 * SpringDataLectureRepository는 실제 lecture 테이블 조회를 담당하는 JPA Repository
 */
public interface SpringDataLectureRepository extends JpaRepository<LectureJpaEntity, Long> {

    // 카테고리 조건 없이 전체 강의를 조회
    Page<LectureJpaEntity> findAll(Pageable pageable);

    // 특정 카테고리 강의만 조회
    Page<LectureJpaEntity> findAllByCategory(LectureCategory category, Pageable pageable);

    /*
     * 특정 ID 목록에 포함된 강의만 조회
     * enrolled=true 조건에서 사용
     */
    Page<LectureJpaEntity> findAllByIdIn(Collection<Long> lectureIds, Pageable pageable);

    // 특정 카테고리이면서, ID 목록에 포함된 강의만 조회
    Page<LectureJpaEntity> findAllByCategoryAndIdIn(
            LectureCategory category,
            Collection<Long> lectureIds,
            Pageable pageable
    );

    /*
     * 특정 ID 목록에 포함되지 않은 강의만 조회
     * enrolled=false 조건에서 사용
     */
    Page<LectureJpaEntity> findAllByIdNotIn(Collection<Long> lectureIds, Pageable pageable);

    /*
     * 특정 카테고리이면서, ID 목록에 포함되지 않은 강의만 조회
     */
    Page<LectureJpaEntity> findAllByCategoryAndIdNotIn(
            LectureCategory category,
            Collection<Long> lectureIds,
            Pageable pageable
    );
}