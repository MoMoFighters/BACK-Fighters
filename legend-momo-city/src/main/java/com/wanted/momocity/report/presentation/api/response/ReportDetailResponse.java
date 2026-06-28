package com.wanted.momocity.report.presentation.api.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.wanted.momocity.report.domain.model.Report;

import java.time.LocalDateTime;

/* comment.
    MS-2 신고 상세 조회 응답용 DTO. ReportListResponse.Item 과 다르게
    resolved(처리 시각) 이 추가된 형태이다.
 */

public record ReportDetailResponse(
        Long reportId,
        Long reporterUserId,
        Long reportedUserId,
        String targetType,
        Long targetId,
        String targetPath,
        String reason,
        String detail,
        boolean isResolved,
        LocalDateTime createdAt,
        LocalDateTime resolvedAt
) {

    public static ReportDetailResponse from(Report report) {
        return new ReportDetailResponse(
                report.getId(),
                report.getReporterUserId(),
                report.getReportedUserId(),
                report.getTargetType().name(),
                report.getTargetId(),
                report.getTargetPath(),
                report.getReason().toKorean(),
                report.getDetail(),
                report.isResolved(),
                report.getCreatedAt(),
                report.getResolvedAt()
        );
    }

}
