package com.wanted.momocity.report.presentation.api.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.wanted.momocity.report.application.usecase.ReportQueryUseCase;
import com.wanted.momocity.report.domain.model.Report;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/* comment.
        MS -1 신고 목록 조회 응답 DTO
        Item 은 신고 1건의 JSON 표현이며, Report 도메인을 표현 계층 형태로 변환
*/
public record ReportListResponse(
        List<Item> items
) {

    // UseCase 출력(ReportList)을 받아 응답 DTO 로 변환
    public static ReportListResponse from(ReportQueryUseCase.ReportList list) {
        List<Item> items = list.reports().stream()
                .map(r -> Item.from(r, list.userNames(), list.targetContents()))
                .toList();
        return new ReportListResponse(items);
    }

    // 신고 1건의 JSON 표현 — 이름·내용 포함
    public record Item(
            Long reportId,
            Long reporterUserId,
            String reporterName,
            String targetType,
            Long targetId,
            String targetContent,
            String reason,
            String detail,
            boolean isResolved,
            LocalDateTime reportedAt
    ) {

        // Report 도메인 + 이름·내용 Map 을 받아 Item 으로 변환
        // targetContents 키가 String 복합 키(타입_id)로 바뀌어 파라미터 타입 일치
        public static Item from(Report report, Map<Long, String> userNames, Map<String, String> targetContents) {
            return new Item(
                    report.getId(),
                    report.getReporterUserId(),
                    userNames.getOrDefault(report.getReporterUserId(), "알 수 없음"),
                    report.getTargetType().name(),
                    report.getTargetId(),
                    targetContents.getOrDefault(report.getTargetType().name() + "_" + report.getTargetId(), null),
                    report.getReason().toKorean(),
                    report.getDetail(),
                    report.isResolved(),
                    report.getCreatedAt()
            );
        }
    }
}