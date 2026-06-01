package com.wanted.momocity.report.domain.repository;

import com.wanted.momocity.report.domain.model.Report;
import com.wanted.momocity.report.domain.model.ReportStatus;

import java.util.List;

/* comment.
    ReportRepository 정리
    1. 해당 클래스가 하는 일 : 신고(Report) 도메인 영속화 계약.
    2. 도메인 계층에 인터페이스를 둠
       → DIP. 도메인이 약속만 정의, 인프라가 구현.
    3. limit(N) 방식
       → 관리자 위젯이 작아 페이지네이션 불필요 (ErrorLogRepository 와 동일 정책).
    4. findById / countByStatus 미선언
       → YAGNI. module04 검토 기능 들어갈 때 추가.
 */
public interface ReportRepository {

    // 신규 신고 저장
    Report save(Report report);

    // 최근 신고 N개 (reportedAt DESC)
    List<Report> findRecent(int limit);

    // 특정 상태의 최근 신고 N개
    List<Report> findByStatus(ReportStatus status, int limit);

    // 전체 신고 수 (대시보드 통계용 - ReportStatsAdapter 가 호출)
    long countAll();
}