package com.wanted.momocity.report.domain.repository;

import com.wanted.momocity.report.domain.model.Report;
import java.util.List;
import java.util.Optional;

/* comment.
    ReportRepository 정리
    도메인 계층의 저장소 계약 인터페이스이다.
 */
public interface ReportRepository {

    // 페이지네이션 결과 묶음 record : Spring 타입 없이 순수 도메인으로 메타데이터 전달
    record ReportPage(List<Report> reports, long totalElements) {}

    // 신규 신고 저장
    Report save(Report report);

    // limit 방식 -> page/size 방식으로 시그니처 변경
    ReportPage findRecent(int page, int size);

    // 동일하게 page/size 로 변경
    ReportPage findByIsResolved(boolean isResolved, int page, int size);

    // 미처리 신고 수 (대시보드 통계용 - ReportStatsAdapter 가 호출)
    long countUnresolved();

    // ID 로 신고 단건 조회 (없으면 빈 Optional)
    Optional<Report> findById(Long id);
}