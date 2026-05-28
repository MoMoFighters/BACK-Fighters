package com.wanted.momocity.report.application.service;

import com.wanted.momocity.report.application.command.SubmitReportCommand;
import com.wanted.momocity.report.application.port.ReporterAccountPort;
import com.wanted.momocity.report.application.usecase.ReportCommandUseCase;
import com.wanted.momocity.report.domain.model.Report;
import com.wanted.momocity.report.domain.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/* comment.
    ReportCommandService 정리
    1. 역할 : ReportCommandUseCase 의 실제 구현체이다. 신고 접수 비즈니스 로직 담당
    2. 위치 : 응용 계층 - 구현
    3. WHY @Service + @Transactional (readOnly 아님)
       → @Service : Spring 이 빈으로 등록, Controller 에 주입됨
       → @Transactional : 쓰기 트랜젝션 (save 호출 -> DB 변경)
    4. WHY @RequiredArgsConstructor (Lombok)
       → final 필드 받는 생성자 자동 생성
       → 의존성이 늘어나더라도 생성자 코드 건드리지 않게 된다.
    5. 의존성 2개의 역할
       - ReporterAccountPort : email -> userId 변환
       - ReportRepository : 도메인 Report 객체 저장
    6. submitReport 처리 흐름 4단계
        a) command.reporterEmail() → ReporterAccountPort.getReporterId() → reporterUserId 획득
        b) Report.submit() 정적 팩토리로 도메인 객체 생성 (PENDING + now)
        c) reportRepository.save() 로 영속화
        d) 저장된 Report (id 부여됨) 반환
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ReportCommandService implements ReportCommandUseCase {

    private final ReporterAccountPort reporterAccountPort;
    private final ReportRepository reportRepository;

    @Override
    public Report submitReport(SubmitReportCommand command) {
        // 1. email → reporterUserId 변환 (외부 BC 호출)
        Long reporterUserId = reporterAccountPort.getReporterId(command.reporterEmail());

        // 2. 도메인 정적 팩토리로 신규 Report 생성 (status=PENDING, reportedAt=now)
        Report report = Report.submit(
                reporterUserId,
                command.targetType(),
                command.targetId(),
                command.reason(),
                command.detail()
        );

        // 3. 저장
        return reportRepository.save(report);
    }
}