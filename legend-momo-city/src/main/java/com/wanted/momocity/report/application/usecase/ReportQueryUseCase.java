package com.wanted.momocity.report.application.usecase;

import com.wanted.momocity.report.domain.model.Report;
import com.wanted.momocity.report.domain.model.ReportStatus;

import java.util.List;

/* comment.
    ReportQueryUseCase 정리
    1. 역할 : 신고 목록 조회(읽기) 응용 계층 계약
    2. 위치 : 응용 계층 - 계약
    3. WHY Query 전용 (Command 와 분리)
        Query: 데이터 조회 → 읽기 전용 트랜잭션 (@Transactional(readOnly = true))
        Command: 데이터 변경 → 쓰기 트랜잭션
        CQRS 경량 적용 → 책임/트랜잭션 정책 분리
    4. WHY 메서드 2개 (getRecent + getByStatus)
        getRecent : 전체 최근 N개 (status 필터 없음)
        getByStatus : 특정 상태(예: PENDING) 의 최근 N개
    5. WHY ReportList record 를 내부에 두는가
        UseCase 의 출력 계약을 같은 파일에 두면 한눈에 파악
        ErrorLogQueryUseCase 의 ErrorLogList 와 동일 패턴
        외부에서 ReportQueryUseCase.ReportList 로 명확히 참조
    6. WHY 도메인 객체(Report) 그대로 노출
       → UseCase 는 응용 계층의 결과를 그대로 노출하게 된다.
       → DTO 변환 책임은 Controller 가 담당하게 된다.
 */
public interface ReportQueryUseCase {

    ReportList getRecent(int limit);

    ReportList getByStatus(ReportStatus status, int limit);

    /* comment.
        ReportList 정리
        1. 역할 : 신고 목록 데이터 묶음. UseCase 출력 계약
        2. WHY List 만 있는데 record 로 감싸는가
           → 지금은 List<Report> 하나만 있지만 향후 데이터 추가 시 시그니처가 바뀌지 않는다
           → 메서드 반환 타입을 record 로 두면 의미가 명확해진다.
        3. 향후 확장 여지
           → totalCount : 전체 신고 수
           → hasMore : 다음페이지가 있는지
     */
    record ReportList(
            List<Report> reports
    ) { }
}