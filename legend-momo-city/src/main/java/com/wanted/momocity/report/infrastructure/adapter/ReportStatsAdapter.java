package com.wanted.momocity.report.infrastructure.adapter;

import com.wanted.momocity.admin.application.port.ReportStatsPort;
import com.wanted.momocity.report.domain.repository.ReportRepository;
import org.springframework.stereotype.Component;

/* comment.
    ReportStatsAdapter 정리
    admin BC 의 ReportStatsPort 를 report BC 의 ReportRepository 로 구현하는 어댑터

 */
@Component
public class ReportStatsAdapter implements ReportStatsPort {

    private final ReportRepository reportRepository;

    public ReportStatsAdapter(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    @Override
    public long countUnresolved() {
        return reportRepository.countUnresolved();
    }
}
