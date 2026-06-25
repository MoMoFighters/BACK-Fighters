package com.wanted.momocity.report.domain.repository;

import com.wanted.momocity.report.domain.model.Report;
import java.util.List;
import java.util.Optional;

/* comment.
    ReportRepository 정리
    1. 해당 클래스가 하는 일 : 신고(Report) 도메인 영속화 계약.
    2. 도메인 계층에 인터페이스를 둠
       → DIP. 도메인이 약속만 정의, 인프라가 구현.
    3. limit(N) 방식
       → 관리자 위젯이 작아 페이지네이션 불필요 (ErrorLogRepository 와 동일 정책).
 */
public interface ReportRepository {

    // 신규 신고 저장
    Report save(Report report);

    // 최근 신고 N개 (reportedAt DESC)
    List<Report> findRecent(int limit);

    // 처리 여부 기준 최근 신고 N개 (false=미처리, true=처리완료)
    List<Report> findByIsResolved(boolean isResolved, int limit);

    // 전체 신고 수 (대시보드 통계용 - ReportStatsAdapter 가 호출)
    long countAll();

    // ID 로 신고 단건 조회 (없으면 빈 Optional)
    Optional<Report> findById(Long id);
}