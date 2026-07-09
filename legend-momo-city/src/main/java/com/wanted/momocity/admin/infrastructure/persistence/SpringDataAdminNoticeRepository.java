package com.wanted.momocity.admin.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/* comment.
    JpaRepository 를 상속받아 기본 CRUD 를 자동으로 제공받고, 메서드 이름 규칙만 선언하면
    Spring Data 가 SQL 를 자동으로 생성해주는 DB 인터페이스
 */

// JpaRepository 를 상속받아 기본 CRDU
public interface SpringDataAdminNoticeRepository extends JpaRepository<AdminNoticeJpaEntity, Long> {

    // isPinned 값으로 필터링된 목록을 페어링 : Spring Data 가 메서드 이름을 보고 SQL 자동 생성
    Page<AdminNoticeJpaEntity> findByIsPinned(boolean isPinned, Pageable pageable);

    // isPinned = false 인 공지만 최신순 페이지네이션 : 고정 공지 분리 후 일반 공지만 뽑을 때 사용
    Page<AdminNoticeJpaEntity> findByIsPinnedFalseOrderByCreatedAtDesc(Pageable pageable);


    // isPinned=false 인 공지 개수를 페이지 위치와 무관하게 항상 독립적으로 세는 COUNT 쿼리
    long countByIsPinnedFalse();

    // id 목록에 해당하는 공지를 한 번에 삭제 : MS-19 선택 삭제용
    @Modifying
    @Query("DELETE FROM AdminNoticeJpaEntity a WHERE a.id IN :ids")
    void deleteAllByIdIn(@Param("ids") List<Long> ids);

    // isPinned DESC → createdAt DESC 순으로 전체 목록 조회 : 고정 공지가 항상 상단에 오도록 정렬
    @Query("SELECT a FROM AdminNoticeJpaEntity a ORDER BY a.isPinned DESC, a.createdAt DESC")
    Page<AdminNoticeJpaEntity> findAllOrderByIsPinnedFirst(Pageable pageable);

    // isPinned = true 인 공지 단건 조회 : Spring Data 가 메서드 이름 보고 SQL 자동 생성
    Optional<AdminNoticeJpaEntity> findByIsPinnedTrue();

}
