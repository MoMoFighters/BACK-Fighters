package com.wanted.momocity.report.infrastructure.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/* comment.
    SpringDataReportRepository 정리
    Spring Data JPA 가 런타임에 메서드명을 읽어서 쿼리를 자동으로 생성하는 저장소 인터페이스
 */
public interface SpringDataReportRepository extends JpaRepository<ReportJpaEntity, Long> {

    // 최근 N개 조회 (created_at 내림차순) — Adapter에서 PageRequest.of(0, limit)으로 호출
    List<ReportJpaEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // 처리 여부 기준 최근 N개 조회 (created_at 내림차순)
    List<ReportJpaEntity> findAllByIsResolvedOrderByCreatedAtDesc(boolean isResolved, Pageable pageable);
}