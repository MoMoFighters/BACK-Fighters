package com.wanted.momocity.report.application.service;

import com.wanted.momocity.report.application.command.SubmitReportCommand;
import com.wanted.momocity.report.application.usecase.ReportCommandUseCase;
import com.wanted.momocity.report.domain.exception.ReportNotFoundException;
import com.wanted.momocity.report.domain.model.Report;
import com.wanted.momocity.report.domain.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/* comment.
    ReportCommandService 정리
    신고 관련 쓰기 작업 담당하는 서비스.
    Controller -> UseCase 인터페이스 -> 구현치 -> Repository 흐름이다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ReportCommandService implements ReportCommandUseCase {

    private static final Logger log = LoggerFactory.getLogger(ReportCommandService.class);

    private final ReportRepository reportRepository;

    @Override
    public Report submitReport(SubmitReportCommand command) {
        // 1. 신고자 userId 확보 (Controller 가 인증 principal 에서 추출해 Command 로 전달)
        Long reporterUserId = command.reporterUserId();

        // 2. 도메인 정적 팩토리로 신규 Report 생성 (status=PENDING, reportedAt=now)
        Report report = Report.submit(
                reporterUserId,
                command.targetType(),
                command.targetId(),
                command.reportedUserId(),
                command.targetPath(),
                command.reason(),
                command.detail()
        );

        // 3. 저장
        Report saved = reportRepository.save(report);

        // 4. 비즈니스 이벤트 로그 (audit) - AOP 흐름 로그와 별개로 "신고 접수됨" 자체를 마킹
        // 운영/감사 시 grep "[Report]" 로 신고 이벤트만 한 번에 추출 가능
        log.info("[Report] 신고 접수 완료 | reportId={} | reporterId={} | target={}({}) | reason={}",
                saved.getId(),
                reporterUserId,
                command.targetType(),
                command.targetId(),
                command.reason()
        );

        return saved;
    }

    @Override
    public Report resolveReport(Long reportId, Long adminId) {
        // 1. 신고 단건 조회 (없으면 예외 처리)
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ReportNotFoundException(reportId));

        // 2. 도메인 행위 호출 (isRead=true, handledAt=now, handlerAdminId 기록)
        // REFACT : Report.resolve()가 파라미터 없어졌으니까 adminId 가 없어져도 된다.
        report.resolve();

        // 3. 변경된 상태 저장하기
        // Report 는 JPA 엔티티가 아니라 순수 도메인 객체이다. 따라서 JPA의 변경감지가 적용되지 않기 때문이다.
        return reportRepository.save(report);
    }
}