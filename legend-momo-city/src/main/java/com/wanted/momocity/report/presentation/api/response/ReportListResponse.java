package com.wanted.momocity.report.presentation.api.response;

import com.wanted.momocity.report.application.usecase.ReportQueryUseCase;
import com.wanted.momocity.report.domain.model.Report;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/* comment.
        MS-1 신고 목록 조회 응답 DTO
        targetType/targetId/targetContent 제거, 페이지네이션 응답 필드 추가
*/
public record ReportListResponse(
        List<Item> items,
        long totalElements,
        int totalPages,
        int page,
        int size
) {

    // 페이지 메타데이터 포함해서 from() 변환
    public static ReportListResponse from(ReportQueryUseCase.ReportList list) {
        List<Item> items = list.reports().stream()
                .map(r -> Item.from(r, list.userNames()))
                .toList();
        // UseCase record 필드명 변경에 맞춰 accessor 이름도 동일하게 수정
        return new ReportListResponse(items, list.totalElements(), list.totalPages(), list.page(), list.size());
    }

    // 목록용 Item — 불필요한 target 정보 제거
    public record Item(
            Long reportId,
            Long reporterUserId,
            String reporterName,
            String reason,
            String detail,
            boolean isResolved,
            LocalDateTime reportedAt
    ) {

        // Report 도메인 + 이름 Map 을 받아 Item 으로 변환
        public static Item from(Report report, Map<Long, String> userNames) {
            return new Item(
                    report.getId(),
                    report.getReporterUserId(),
                    userNames.getOrDefault(report.getReporterUserId(), "알 수 없음"),
                    report.getReason().toKorean(),
                    report.getDetail(),
                    report.isResolved(),
                    report.getCreatedAt()
            );
        }
    }
}
