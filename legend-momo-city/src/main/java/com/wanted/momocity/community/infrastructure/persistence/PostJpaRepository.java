package com.wanted.momocity.community.infrastructure.persistence;

import com.wanted.momocity.community.domain.model.PostCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/*
* comment.
*  Spring Data JPA 가 구현체를 자동으로 생성
*  -> Domain 을 모르고 JpaEntity 만 다룸
* */

public interface PostJpaRepository extends JpaRepository<PostJpaEntity, Long> {

    // 단건 조회 (소프트딜리트 제외)
    Optional<PostJpaEntity> findByIdAndDeletedAtIsNull(Long PostId);

    /*
    * comment.
    *  게시글 단건 조회 + contents fetch join
    *  -
    *  단건 조회 시 contents 항상 필요
    *  -> LAZY 로딩 시 contents 접근할 때마다 추가 쿼리 발생
    *  -> fetch join 으로 한 번에 조회
    * */

    @Query("""
        SELECT DISTINCT p FROM PostJpaEntity p
        LEFT JOIN FETCH p.contents
        WHERE p.id = :postId
        AND p.deletedAt IS NULL
    """)
    Optional<PostJpaEntity> findByIdWithContents(@Param("postId") Long postId);

    /*
    * comment.
    *  게시글 목록 커서 기반 조회
    *  커서기반 : cursor = null -> 첫 페이지, cursor != null -> 해당 postId 보다 작은 데이터 조회 (내림차순)
    *  카테고리 필터링 : category = null -> 전체 조회, category != null -> 해당 카테고리만 조회
    * */

    @Query("""
    SELECT p FROM PostJpaEntity p
    WHERE p.deletedAt IS NULL
    AND (:category IS NULL OR p.category = :category)
    AND (:cursor IS NULL OR p.id < :cursor)
    ORDER BY p.id DESC
""")
    List<PostJpaEntity> findAllByCategoryWithCursor(
            @Param("category") PostCategory category,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    /*
    * comment.
    *  카테고리별 전체 게시글 수 조회
    *  cursor 기반 페이지네이션 totalCount 반환용
    *  -> category = null -> 전체 게시글 수
    * */

    @Query("""
    SELECT COUNT(p)
    FROM PostJpaEntity p
    WHERE p.deletedAt IS NULL
    AND (:category IS NULL OR p.category = :category)
""")
    int countByCategory(@Param("category") PostCategory category);

    // 유저별 게시글 커서 기반 조회
    // cursor = null -> 첫 페이지, cursor != null -> 해당 postId 보다 작은 데이터 조회
    @Query("""
    SELECT p FROM PostJpaEntity p
    WHERE p.userId = :userId
    AND p.deletedAt IS NULL
    AND (:cursor IS NULL OR p.id < :cursor)
    ORDER BY p.id DESC
""")
    List<PostJpaEntity> findByUserIdWithCursor(
            @Param("userId") Long userId,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    // 유저별 게시글 수 조회
    @Query("""
    SELECT COUNT(p)
    FROM PostJpaEntity p
    WHERE p.userId = :userId
    AND p.deletedAt IS NULL
""")
    int countByUserId(@Param("userId") Long userId);

    // 유저별 총 조회수 합산
    @Query("""
    SELECT COALESCE(SUM(p.viewCount), 0)
    FROM PostJpaEntity p
    WHERE p.userId = :userId
    AND p.deletedAt IS NULL
""")
    int sumViewCountByUserId(@Param("userId") Long userId);

    // 유저별 총 좋아요 수 합산
    @Query("""
    SELECT COALESCE(SUM(p.likeCount), 0)
    FROM PostJpaEntity p
    WHERE p.userId = :userId
    AND p.deletedAt IS NULL
""")
    int sumLikeCountByUserId(@Param("userId") Long userId);

    /*
    * comment.
    *  게시글 키워드 검색
    *  검색 대상 : title, content, authorName
    *  커서기반 : cursor = null -> 첫 페이지, cursor != null -> 해당 postId 보다 작은 데이터 조회
    *  -
    *  와일드카드 이스케이프
    *  사용자 입력 키워드의 %, _ 를 PostRepositoryAdapter.escapeKeyword() 에서 전처리
    *  -> % -> \%, _ -> \_ 로 변환 후 전달
    *  -> JPQL ESCAPE 절 미사용 (Hibernate 6 호환성 문제로 제거)
    * */

    @Query("""
    SELECT DISTINCT p FROM PostJpaEntity p
    LEFT JOIN PostContentJpaEntity c ON c.postId = p.id
    WHERE p.deletedAt IS NULL
    AND (
        p.title LIKE %:keyword%
        OR c.content LIKE %:keyword%
    )
    AND (:category IS NULL OR p.category = :category)
    AND (:cursor IS NULL OR p.id < :cursor)
    ORDER BY p.id DESC
""")
    List<PostJpaEntity> searchByKeyword(
            @Param("keyword") String keyword,
            @Param("category") PostCategory category,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    // 키워드 검색 결과 총 개수
    // DISTINCT 로 중복 제거
    @Query("""
    SELECT COUNT(DISTINCT p.id)
    FROM PostJpaEntity p
    LEFT JOIN PostContentJpaEntity c ON c.postId = p.id
    WHERE p.deletedAt IS NULL
    AND (
        p.title LIKE %:keyword%
        OR c.content LIKE %:keyword%
    )
    AND (:category IS NULL OR p.category = :category)
""")
    int countByKeyword(@Param("keyword") String keyword,
                       @Param("category") PostCategory category);

    /*
    * comment.
    *  유저별 게시글 Id 목록만 조회
    *  getDashboard() 에서 댓글 수 집계용으로 postIds 만 필요
    *  -> Id 만 조회하여 붎필요한 데이터 로드 방지
    * */

    @Query("""
    SELECT p.id FROM PostJpaEntity p
    WHERE p.userId = :userId
    AND p.deletedAt IS NULL
""")
    List<Long> findPostIdsByUserId(@Param("userId") Long userId);

    /*
    * comment.
    *  같은 카테고리 인기 게시글 조회
    *  viewCount * 0.6 + likeCount * 0.4
    *  현재 게시글 제외
    * */

    @Query("""
    SELECT p FROM PostJpaEntity p
    WHERE p.deletedAt IS NULL
    AND p.category = :category
    AND p.id != :postId
    ORDER BY (p.viewCount * 0.6 + p.likeCount * 0.4) DESC
""")
    List<PostJpaEntity> findTopPostsByCategory(
            @Param("category") PostCategory category,
            @Param("postId") Long postId,
            Pageable pageable
    );

    /*
    * comment.
    *  같은 작성자의 최신 게시글 조회
    *  현재 게시글 제외, 추천 게시글 제외, 최신
    * */

    @Query("""
    SELECT p FROM PostJpaEntity p
    WHERE p.deletedAt IS NULL
    AND p.userId = :userId
    AND p.id != :postId
    AND p.id NOT IN :excludeIds
    ORDER BY p.createdAt DESC
""")
    List<PostJpaEntity> findLatestPostsByAuthor(
            @Param("userId") Long userId,
            @Param("postId") Long postId,
            @Param("excludeIds") List<Long> excludeIds,
            Pageable pageable
    );

    /*
     * comment.
     *  해당 연도의 월별 게시글 수 조회
     *  소프트딜리트 제외
     *  1월~12월 데이터 없는 달은 결과에 미포함 -> PostStatsAdapter 에서 0으로 채움
     * */

    @Query("""
    SELECT FUNCTION('MONTH', p.createdAt) AS month, COUNT(p) AS count
    FROM PostJpaEntity p
    WHERE p.deletedAt IS NULL
    AND FUNCTION('YEAR', p.createdAt) = :year
    GROUP BY FUNCTION('MONTH', p.createdAt)
    ORDER BY FUNCTION('MONTH', p.createdAt) ASC
""")
    List<Object[]> countPostByMonth(@Param("year") int year);

    // 하드딜리트 (스케줄러용)
    // deletedAt IS NOT NULL : 소프트딜리트된 게시글만 대상
    // deletedAt < threshold : 기준일(6개월) 이전 데이터만 삭제
    @Modifying
    @Query("DELETE FROM PostJpaEntity p WHERE p.deletedAt IS NOT NULL AND p.deletedAt < :threshold")
    int hardDeleteByDeletedAtBefore(@Param("threshold") LocalDateTime threshold);

}
