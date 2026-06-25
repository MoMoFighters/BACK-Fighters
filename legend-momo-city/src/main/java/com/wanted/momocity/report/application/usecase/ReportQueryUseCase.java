package com.wanted.momocity.report.application.usecase;

import com.wanted.momocity.report.domain.model.Report;
import java.util.List;

/* comment.
    ReportQueryUseCase 정리
    신고 조회 기능의 응용 계층 계약
    getRecent : 최근 N 개 전체 조회
    getByIsResolved : 처리 여부 (isResolved) 기준 필터 조회
    getById : 신고 단건 조회
 */
public interface ReportQueryUseCase {

    ReportList getRecent(int limit);
    ReportList getByIsResolved(boolean isResolved, int limit);

    // id 값을 받기 위해서 필요한 data
    ReportDetail getById(Long id);

    record ReportList(
            List<Report> reports
    ) { }

    // 해당 UseCase 가 어떤 것을 반환하는지 확인가능하기 때문에 여기에 둔다.
    record ReportDetail(
            Report report
    ) {}
}