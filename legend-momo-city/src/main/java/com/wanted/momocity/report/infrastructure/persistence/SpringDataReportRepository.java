package com.wanted.momocity.report.infrastructure.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/* comment.
    SpringDataReportRepository 정리
    1. 역할 : Spring Data Jpa 가 런타임에 자동으로 구현체를 만들어주는 저장소 인터페이스
    2. 위치 : 인프라 계층
    3. 누가 호출하는가 : ReportRepositoryAdapter 가 주입받아서 호출하게 된다. (외부에 노출되지 않는다)
    4. WHY 도메인 ReportRepository 와 별도로 두는가
       → ReportRepository : Report 도메인 모델을 다룬다
       → Spring 기술 인터페이스, ReportJpaEntity 를 다룬다.
       → 둘을 이어주는 다리가 ReportRepositoryAdapter 변환하고 격리하는 담당한다.
    5. WHY Pageable 을 limit 처럼 쓰는가
       → 전체 페이지네이션은 불필요하기 때문이다.
 */
public interface SpringDataReportRepository extends JpaRepository<ReportJpaEntity, Long> {

    // 최근 N개 (reportedAt DESC) - Adapter 에서 PageRequest.of(0, limit) 으로 호출
    List<ReportJpaEntity> findAllByOrderByReportedAtDesc(Pageable pageable);

    // 특정 상태의 최근 N개 (reportedAt DESC)
    List<ReportJpaEntity> findAllByStatusOrderByReportedAtDesc(String status, Pageable pageable);
}