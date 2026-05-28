package com.wanted.momocity.report.application.usecase;

import com.wanted.momocity.report.application.command.SubmitReportCommand;
import com.wanted.momocity.report.domain.model.Report;

/* comment.
    ReportCommandUseCase 정리
    1. 역할 : 신고 접수 응용 계층 계약. Controller 가 의존할 인터페이스이다.
    실제 구현은 Service 계층에서 하게 된다.
    2. 위치 : 응용 계층 - 계약
    3. WHY UseCase 인터페이스로 분리
       → Controller 가 Service 구현체를 직접 의존하면 DIP 흐름 위반
       → {DIP 흐름 controller -> usecase 인터페이스 <- service
       → 인터페이스만 의존 시 구현체 교체 / 테스트 시 Mock 주입 편의
       → enrollment 의 EnrollmentCommandUseCase 와 동일한 정책
    4. WHY Command/Query 분리 (CQRS 경량 버전)
       → command : 데이터 변경
       → query : 데이터 조회
    5. WHY Report 도메인 객체를 그대로 반환 (DTO 변환 X)
       → UseCase 는 도메인 영역의 결과를 그대로 노출하게 된다.
       → DTO 변환 책임은 Controller -> 표현 계층이 HTTP 응답 형태가 결정된다.
 */
public interface ReportCommandUseCase {

    Report submitReport(SubmitReportCommand command);
}