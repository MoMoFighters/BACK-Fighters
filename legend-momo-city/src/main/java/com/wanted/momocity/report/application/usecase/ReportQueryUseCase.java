package com.wanted.momocity.report.application.usecase;

import com.wanted.momocity.report.domain.model.Report;
import java.util.List;
import java.util.Map;

/* comment.
    ReportQueryUseCase 정리
    신고 조회 기능의 응용 계층 계약
    getRecent : 최근 N 개 전체 조회
    getByIsResolved : 처리 여부 (isResolved) 기준 필터 조회
    getById : 신고 단건 조회
 */
public interface ReportQueryUseCase {

    ReportList getRecent(int page, int size);
    ReportList getByIsResolved(boolean isResolved, int page, int size);

    // id 값을 받기 위해서 필요한 data
    ReportDetail getById(Long id);

    // targetContents 제거, 페이지 메타데이터 4개 추가
    record ReportList(
            List<Report> reports,
            Map<Long, String> userNames,
            long totalElements,
            int totalPages,
            // FE 컨벤션 통일 — 접근로그(page/size) 기준으로 필드명 맞춤
            int page,
            int size
    ) {}

    // 해당 UseCase 가 어떤 것을 반환하는지 확인가능하기 때문에 여기에 둔다.
    record ReportDetail(
            Report report,
            String reporterName,
            String reportedName,
            String targetContent,
            boolean isDeleted,
            // CHAPTER 타입일 때 lectureId 담아준다. 나머지는 null 값
            Long parentId
    ) {}
}