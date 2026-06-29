package com.wanted.momocity.report.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/* comment.
    SpringDataReportRepository 정리
    Spring Data JPA 가 런타임에 메서드명을 읽어서 쿼리를 자동으로 생성하는 저장소 인터페이스
 */
public interface SpringDataReportRepository extends JpaRepository<ReportJpaEntity, Long> {

    // 페이지네이션 포함 전체 목록 — Page 로 반환해 totalElements 확보
    // List → Page 반환으로 변경해 totalElements 포함
    Page<ReportJpaEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // 처리 여부 기준도 동일하게 Page 반환
    Page<ReportJpaEntity> findAllByIsResolvedOrderByCreatedAtDesc(boolean isResolved, Pageable pageable);

    // reportedUserId 는 컬럼 기준으로 해당 유저가 신고 당한 내역을 전부 조회
    List<ReportJpaEntity> findAllByReportedUserId(Long reportedUserId);

    // 미처리 신고 수 (is_resolved = false) — ReportStatsAdapter 가 countUnresolved() 구현에 사용
    long countByIsResolved(boolean isResolved);
    
}