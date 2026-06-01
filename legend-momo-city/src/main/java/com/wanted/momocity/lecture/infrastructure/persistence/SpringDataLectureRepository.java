package com.wanted.momocity.lecture.infrastructure.persistence;

import com.wanted.momocity.lecture.domain.model.LectureCategory;
import com.wanted.momocity.lecture.domain.model.LectureStatus;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

// SpringDataLectureRepository는 실제 lecture 테이블 조회를 담당하는 JPA Repository
public interface SpringDataLectureRepository extends JpaRepository<LectureJpaEntity, Long> {

    // 강의 상태 조건으로 강의 목록을 조회
    Page<LectureJpaEntity> findAllByStatus(
            LectureStatus status,
            Pageable pageable
    );

    // 카테고리와 강의 상태 조건으로 강의 목록을 조회
    Page<LectureJpaEntity> findAllByCategoryAndStatus(
            LectureCategory category,
            LectureStatus status,
            Pageable pageable
    );

    // 특정 ID 목록에 포함되고, 특정 상태인 강의만 조회
// enrolled=true 조건에서 사용합니다.
    Page<LectureJpaEntity> findAllByStatusAndIdIn(
            LectureStatus status,
            Collection<Long> lectureIds,
            Pageable pageable
    );

    // 카테고리 조건까지 함께 적용해서, 특정 ID 목록에 포함된 강의만 조회
    Page<LectureJpaEntity> findAllByCategoryAndStatusAndIdIn(
            LectureCategory category,
            LectureStatus status,
            Collection<Long> lectureIds,
            Pageable pageable
    );

    // 특정 ID 목록에 포함되지 않고, 특정 상태인 강의만 조회
// enrolled=false 조건에서 사용합니다.
    Page<LectureJpaEntity> findAllByStatusAndIdNotIn(
            LectureStatus status,
            Collection<Long> lectureIds,
            Pageable pageable
    );

    // 카테고리 조건까지 함께 적용해서, 특정 ID 목록에 포함되지 않은 강의만 조회
    Page<LectureJpaEntity> findAllByCategoryAndStatusAndIdNotIn(
            LectureCategory category,
            LectureStatus status,
            Collection<Long> lectureIds,
            Pageable pageable
    );

    /* comment
     * 특정 상태의 강의 개수를 조회한다.
     * 관리자 대시보드에서 현재 진행 중인 강의 수를 계산할 때 사용한다.
     */
    long countByStatus(LectureStatus status);

    /* comment
     * 특정 날짜 이전에 생성된 특정 상태의 강의 개수를 조회한다.
     * 관리자 대시보드에서 이전 기간 대비 증감률을 계산할 때 사용한다.
     */
    long countByStatusAndCreatedAtBefore(
            LectureStatus status,
            LocalDateTime createdAt
    );

    /* comment
     * 강사가 본인이 등록한 강의 목록을 조회합니다.
     *  Query문 쓴 이유 :  선택 필터가 있는 목록 조회를 메서드 여러 개로 나누지 않고, 하나의 조회 메서드에서 처리하기 위해서
     * 조회 결과로 LectureJpaEntity 객체 전체를 가져와서 LectureJpaEntity에서 데이터를 조회하고,
     * 그 Entity를 앞으로 l이라는 별명으로 부른다.
     * 로그인한 강사의 ID와 강의의 teacherId가 같은 것만 조회한다.
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

    /* comment
     * 관리자가 강의 목록을 조회
     *
     * 조건:
     * - statuses 안에 포함된 강의 상태만 조회한다.
     *   예: WAITING, ACTIVE
     * - category가 있으면 해당 카테고리만 조회한다.
     * - keyword가 있으면 강의 제목에 keyword가 포함된 강의만 조회한다.
     * - 최신 등록순으로 정렬한다.
     */
    @Query("""
    select l
    from LectureJpaEntity l
    where l.status in :statuses
      and (:category is null or l.category = :category)
      and (:keyword is null or l.title like concat('%', :keyword, '%'))
    order by l.createdAt desc
    """)
    Page<LectureJpaEntity> findAdminLectures(
            @Param("statuses") List<LectureStatus> statuses,
            @Param("category") LectureCategory category,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    /* comment
     * 학생용 강의 목록을 조회합니다.
     *
     * 조건:
     * - ACTIVE 상태 강의만 조회합니다.
     * - category가 있으면 해당 카테고리만 조회합니다.
     * - keyword가 있으면 강의 제목으로 검색합니다.
     *
     * teacherName 검색은 user 테이블 조인이 필요하므로 다음 단계에서 확장합니다.
     */
    @Query("""
    select l
    from LectureJpaEntity l
    where l.status = :status
      and (:category is null or l.category = :category)
      and (:keyword is null or l.title like concat('%', :keyword, '%'))
    order by l.createdAt desc
    """)
    Page<LectureJpaEntity> findStudentLectures(
            @Param("status") LectureStatus status,
            @Param("category") LectureCategory category,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    /* comment
     * 학생용 강의 목록 중,
     * 로그인한 사용자가 이미 수강신청한 강의만 조회합니다.
     *
     * enrolled=true 조건에서 사용합니다.
     */
    @Query("""
    select l
    from LectureJpaEntity l
    where l.status = :status
      and l.id in :lectureIds
      and (:category is null or l.category = :category)
      and (:keyword is null or l.title like concat('%', :keyword, '%'))
    order by l.createdAt desc
    """)
    Page<LectureJpaEntity> findStudentLecturesByEnrolled(
            @Param("status") LectureStatus status,
            @Param("lectureIds") Collection<Long> lectureIds,
            @Param("category") LectureCategory category,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    /* comment
     * 학생용 강의 목록 중,
     * 로그인한 사용자가 아직 수강신청하지 않은 강의만 조회합니다.
     *
     * enrolled=false 조건에서 사용합니다.
     */
    @Query("""
    select l
    from LectureJpaEntity l
    where l.status = :status
      and l.id not in :lectureIds
      and (:category is null or l.category = :category)
      and (:keyword is null or l.title like concat('%', :keyword, '%'))
    order by l.createdAt desc
    """)
    Page<LectureJpaEntity> findStudentLecturesByNotEnrolled(
            @Param("status") LectureStatus status,
            @Param("lectureIds") Collection<Long> lectureIds,
            @Param("category") LectureCategory category,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}