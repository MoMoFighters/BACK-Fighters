package com.wanted.momocity.report.application.service;

import com.wanted.momocity.report.application.command.SubmitReportCommand;
import com.wanted.momocity.report.application.usecase.ReportCommandUseCase;
import com.wanted.momocity.report.domain.model.Report;
import com.wanted.momocity.report.domain.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    5. 의존성 1개의 역할
       - ReportRepository : 도메인 Report 객체 저장
       (신고자 userId 는 Controller 가 인증 principal 에서 추출해 Command 로 전달 → 외부 BC 조회 불필요)
    6. submitReport 처리 흐름 4단계
        a) command.reporterUserId() 로 신고자 id 확보 (인증 principal 출처)
        b) Report.submit() 정적 팩토리로 도메인 객체 생성 (PENDING + now)
        c) reportRepository.save() 로 영속화
        d) 저장된 Report (id 부여됨) 반환
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
}