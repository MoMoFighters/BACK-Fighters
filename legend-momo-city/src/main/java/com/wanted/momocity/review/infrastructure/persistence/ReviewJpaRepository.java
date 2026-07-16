package com.wanted.momocity.review.infrastructure.persistence;

import com.wanted.momocity.review.domain.model.ReviewStatus;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ReviewJpaRepository extends JpaRepository<ReviewJpaEntity, Long> {

    boolean existsByUserIdAndLectureIdAndStatus(Long userId, Long lectureId, ReviewStatus status);

    Optional<ReviewJpaEntity> findByIdAndStatus(Long id, ReviewStatus status);
    
    // 강의 별 리뷰 통계 조회 결과 받기
    interface ReviewStatsProjection {
        Long getLectureId();
        Double getAverageRating();
        Long getReviewCount();
    }

    // 해당 강의의 rating 평균을 구하고 없으면 0 반환
    @Query("select coalesce(avg(r.rating), 0) " +
            "from ReviewJpaEntity r" +
            " where r.lectureId = :lectureId and r.status = 'ACTIVE'") // ACTIVE 수강평만 평균 계산
    // 특정 강의의 평균 평점 조회
    double findAverageRatingByLectureId(@Param("lectureId") Long lectureId);

    // 전달받은 강의 ID 목록에 속한 ACTIVE 수강평의 전체 평균 별점을 계산합니다.
    @Query("""
            select coalesce(avg(r.rating), 0)
            from ReviewJpaEntity r
            where r.lectureId in :lectureIds
              and r.status = 'ACTIVE'
            """)
    // 평균 계산 결과를 double 값으로 반환합니다.
    double findAverageRatingByLectureIds(
            // JPQL의 :lectureIds에 ACTIVE 강의 ID 목록을 연결합니다.
            @Param("lectureIds") List<Long> lectureIds
    );

    // 특정 강의 수강평을 최신순
    Page<ReviewJpaEntity> findAllByLectureIdAndStatusOrderByCreatedAtDesc(Long lectureId,ReviewStatus status, Pageable pageable);

    long countByLectureIdAndStatus(Long lectureId, ReviewStatus status);


    @Query("""
            select r.lectureId as lectureId,
                   coalesce(avg(r.rating), 0) as averageRating,
                   count(r.id) as reviewCount
            from ReviewJpaEntity r
            where r.lectureId in :lectureIds
              and r.status = 'ACTIVE'
            group by r.lectureId
        """) // 여러 강의 ID에 대해 강의별 평균 평점과 리뷰 개수를 한 번에 조회
    List<ReviewStatsProjection> findReviewStatsByLectureIds(@Param("lectureIds") List<Long> lectureIds); // 강의 ID 목록 기준 리뷰 통계 조회

    // 특정 강의의 ACTIVE 상태 수강평 원문만 최신순으로 조회합니다.
    @Query("""
        select r.content
        from ReviewJpaEntity r
        where r.lectureId = :lectureId
          and r.status = 'ACTIVE'
        order by r.createdAt desc
        """)
    List<String> findContentsByLectureId(@Param("lectureId") Long lectureId); // 수강평이 없으면 빈 리스트를 반환합니다.
}
