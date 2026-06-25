package com.wanted.momocity.report.presentation.api.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.wanted.momocity.report.application.usecase.ReportQueryUseCase;
import com.wanted.momocity.report.domain.model.Report;

import java.time.LocalDateTime;
import java.util.List;

/* comment.
        MS -1 신고 목록 조회 응답 DTO
        Item 은 신고 1건의 JSON 표현이며, Report 도메인을 표현 계층 형태로 변환
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


    public record Item(
            Long reportId,
            Long reporterUserId,
            String targetType,
            Long targetId,
            String reason,
            String detail,
            boolean isResolved,
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
                    report.isResolved(),
                    report.getCreatedAt()
            );
        }
    }
}