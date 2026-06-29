package com.wanted.momocity.report.infrastructure.adapter;

import com.wanted.momocity.admin.application.port.PendingReportPort;
import com.wanted.momocity.report.domain.repository.ReportRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/* comment.
    PendingReportPort 구현체 - admin BC 포트를 report BC 가 어댑터로 제공
 */

@Component
public class PendingReportAdapter implements PendingReportPort {

    private final ReportRepository reportRepository;

    public PendingReportAdapter(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    @Override
    public List<PendingReportItem> getPending(int limit) {
        // ReportRepository 시그니처가 page/size 로 변경됨 — 대시보드용이라 page=1 고정
        return reportRepository.findByIsResolved(false, 1, limit).reports()
                .stream()
                .map(report -> new PendingReportItem(
                        report.getId(),
                        report.getReporterUserId(),
                        report.getReason().toKorean(),
                        report.getCreatedAt()
                ))
                .toList();
    }
}
