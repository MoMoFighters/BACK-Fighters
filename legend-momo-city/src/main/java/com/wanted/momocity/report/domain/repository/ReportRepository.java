package com.wanted.momocity.report.domain.repository;

import com.wanted.momocity.report.domain.model.Report;
import java.util.List;
import java.util.Optional;

/* comment.
    ReportRepository 정리
    도메인 계층의 저장소 계약 인터페이스이다.
 */
public interface ReportRepository {

    // 신규 신고 저장
    Report save(Report report);

    // 최근 신고 N개 (reportedAt DESC)
    List<Report> findRecent(int limit);

    // 처리 여부 기준 최근 신고 N개 (false=미처리, true=처리완료)
    List<Report> findByIsResolved(boolean isResolved, int limit);

    // 미처리 신고 수 (대시보드 통계용 - ReportStatsAdapter 가 호출)
    long countUnresolved();

    // ID 로 신고 단건 조회 (없으면 빈 Optional)
    Optional<Report> findById(Long id);
}