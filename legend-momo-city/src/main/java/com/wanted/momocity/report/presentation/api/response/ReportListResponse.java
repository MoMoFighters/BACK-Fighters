package com.wanted.momocity.report.presentation.api.response;

import com.wanted.momocity.report.application.usecase.ReportQueryUseCase;
import com.wanted.momocity.report.domain.model.Report;

import java.time.LocalDateTime;
import java.util.List;

/* comment.
    ReportListResponse 정리
    1. 역할 : 신고 목록을 HTTP 응답을 보내는 DTO이다. AdminReportController 가 도메인 리스트를 이걸로 변환해서 반환
    2. 위치 : 표현 계층 - 출력 DTO
    3. WHY wrapper 패턴 (items 한 필드만)
       → 향후 totalCount, hasMore 등 데이터 추가 시 시그니처 변경 X
    4. WHY from(ReportList) 정적 팩토리
       → UseCase 출력(ReportQueryUseCase.ReportList) → 응답 DTO 변환 책임 응집
       → Controller 가 ReportListResponse.from(list) 한 줄로 변환
    5. 응용 ReportList 와 분리하는 이유
        → ReportList (응용) : 도메인 List<Report> 묶음, UseCase 출력 계약
        → ReportListResponse (표현) : HTTP 직렬화 형태, FE 가 받는 JSON 구조
        → 두 계층 다른 책임 → 분리
    6. WHY 내부 Item record 분리
       → List<Report> 직접 노출 X → 표현 계층은 도메인 노출 안 함
 */
public record ReportListResponse(
        List<Item> items
) {

    public static ReportListResponse from(ReportQueryUseCase.ReportList list) {
        List<Item> items = list.reports().stream()
                .map(Item::from)
                .toList();
        return new ReportListResponse(items);
    }

    /* comment.
        Item 정리
        1. 역할 : 신고 1건의 응답 형태이며 목록 안에 여러 개가 들어가게 된다.
        2. WHY enum 을 String 으로 변환 (targetType, reason, status)
           → 표현 계층은 enum 타입이 직접 노출되지 않는다.
           → JSON 직렬화 시 FE 호환성이 높아진다.
        3. 필드 8개 의미
        reportId : 신고 식별자
        reporterUserId : 신고자 ID
        targetType : 신고 대상 종류 (POST / COMMENT / USER / LECTURE)
        targetId : 신고 대상 ID
        reason : 신고 사유
        detail : 자유 설명 (nullable)
        status : 신고 처리 상태
        reportedAt : 접수 시각
     */
    public record Item(
            Long reportId,
            Long reporterUserId,
            String targetType,
            Long targetId,
            String reason,
            String detail,
            String status,
            LocalDateTime reportedAt
    ) {

        public static Item from(Report report) {
            return new Item(
                    report.getId(),
                    report.getReporterUserId(),
                    report.getTargetType().name(),
                    report.getTargetId(),
                    report.getReason().name(),
                    report.getDetail(),
                    report.getStatus().name(),
                    report.getReportedAt()
            );
        }
    }
}