package com.wanted.momocity.report.application.usecase;

import com.wanted.momocity.report.application.command.SubmitReportCommand;
import com.wanted.momocity.report.domain.model.Report;

/* comment.
    ReportCommandUseCase 정리
    신고 관련 쓰기 작업 게약 : Controller 가 의존하는 인터페이스, 구현은 ReportCommandService
 */
public interface ReportCommandUseCase {

    // 신고 접수
    Report submitReport(SubmitReportCommand command);

    // 둘다 신고 BC 의 데이터를 변경하는 Command 작업
    // REFACT : 신고 처리 완료 (adminId 제거 파라미터가 없어졌음)
    Report resolveReport(Long reportId);
}