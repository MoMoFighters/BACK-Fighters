package com.wanted.momocity.lecture.infrastructure.persistence;

import com.wanted.momocity.lecture.domain.model.LectureCategory;
import com.wanted.momocity.lecture.domain.model.LectureStatus;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;

/**
 * SpringDataLectureRepository는 실제 lecture 테이블 조회를 담당하는 JPA Repository
 */
public interface SpringDataLectureRepository extends JpaRepository<LectureJpaEntity, Long> {

    // 강의 상태 조건으로 강의 목록을 조회합니다.
    Page<LectureJpaEntity> findAllByStatus(
            LectureStatus status,
            Pageable pageable
    );

    // 카테고리와 강의 상태 조건으로 강의 목록을 조회합니다.
    Page<LectureJpaEntity> findAllByCategoryAndStatus(
            LectureCategory category,
            LectureStatus status,
            Pageable pageable
    );

    // 특정 ID 목록에 포함되고, 특정 상태인 강의만 조회합니다.
// enrolled=true 조건에서 사용합니다.
    Page<LectureJpaEntity> findAllByStatusAndIdIn(
            LectureStatus status,
            Collection<Long> lectureIds,
            Pageable pageable
    );

    // 카테고리 조건까지 함께 적용해서, 특정 ID 목록에 포함된 강의만 조회합니다.
    Page<LectureJpaEntity> findAllByCategoryAndStatusAndIdIn(
            LectureCategory category,
            LectureStatus status,
            Collection<Long> lectureIds,
            Pageable pageable
    );

    // 특정 ID 목록에 포함되지 않고, 특정 상태인 강의만 조회합니다.
// enrolled=false 조건에서 사용합니다.
    Page<LectureJpaEntity> findAllByStatusAndIdNotIn(
            LectureStatus status,
            Collection<Long> lectureIds,
            Pageable pageable
    );

    // 카테고리 조건까지 함께 적용해서, 특정 ID 목록에 포함되지 않은 강의만 조회합니다.
    Page<LectureJpaEntity> findAllByCategoryAndStatusAndIdNotIn(
            LectureCategory category,
            LectureStatus status,
            Collection<Long> lectureIds,
            Pageable pageable
    );

    /*
     * 강사가 본인이 등록한 강의 목록을 조회합니다.
     *
     * 조건:
     * - teacherId는 필수입니다.
     * - category가 있으면 해당 카테고리만 조회합니다.
     * - keyword가 있으면 강의 제목에 keyword가 포함된 강의만 조회합니다.
     */
    @Query("""
        select l
        from LectureJpaEntity l
        where l.teacherId = :teacherId
          and (:category is null or l.category = :category)
          and (:keyword is null or l.title like concat('%', :keyword, '%'))
        order by l.createdAt desc
        """)
    Page<LectureJpaEntity> findTeacherLectures(
            @Param("teacherId") Long teacherId,
            @Param("category") LectureCategory category,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}