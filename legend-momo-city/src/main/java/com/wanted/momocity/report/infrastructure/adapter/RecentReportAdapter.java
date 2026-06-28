package com.wanted.momocity.report.infrastructure.adapter;

/* comment.
    RecentReportPort 구현체
    admin BC 포트를 report BC 가 어댑터로 제공한다.
 */

import com.wanted.momocity.admin.application.port.RecentReportPort;
import com.wanted.momocity.report.domain.repository.ReportRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RecentReportAdapter implements RecentReportPort {

    private final ReportRepository reportRepository;

    public RecentReportAdapter(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    @Override
    public List<RecentReportItem> getRecent(int limit) {
        return reportRepository.findRecent(limit)
                .stream()
                .map(report -> new RecentReportItem(
                        report.getId(),
                        report.getReporterUserId(),
                        report.getReason().toKorean(),
                        report.isResolved(),
                        report.getCreatedAt()
                ))
                .toList();
    }
}
