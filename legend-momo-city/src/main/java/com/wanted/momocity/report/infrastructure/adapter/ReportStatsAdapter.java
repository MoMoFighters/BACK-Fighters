package com.wanted.momocity.report.infrastructure.adapter;

import com.wanted.momocity.admin.application.port.ReportStatsPort;
import com.wanted.momocity.report.domain.repository.ReportRepository;
import org.springframework.stereotype.Component;

/* comment.
    ReportStatsAdapter 정리
    1. 역할 : admin BC 의 ReportStatsPort 를 report BC 의 ReportRepository 로 구현하는 어댑터
    2. 위치 : report/infrastructure/adapter (인프라 계층 - 외부 BC Port 구현)
    3. WHY report BC 에 위치 (admin 아님)
       → 데이터 소유자(report) 가 어댑터를 제공하는 패턴
       → member / lecture 가 자기 어댑터 제공하는 것과 동일 정책
       → admin BC 는 ReportStatsPort 인터페이스만 보고 구현체 모름
    4. WHY ReportRepository 직접 사용
       → 자체 BC 내부 의존 → 도메인 Repository 직접 사용 OK
       → ReporterAccountPort 와 다른 점 : ReporterAccountPort 는 외부 BC(auth) 사용이라 Port 패턴
 */
@Component
public class ReportStatsAdapter implements ReportStatsPort {

    private final ReportRepository reportRepository;

    public ReportStatsAdapter(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    @Override
    public long countAll() {
        return reportRepository.countAll();
    }
}
