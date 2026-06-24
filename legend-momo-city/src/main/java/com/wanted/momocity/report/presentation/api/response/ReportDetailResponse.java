package com.wanted.momocity.report.presentation.api.response;

import com.wanted.momocity.report.domain.model.Report;

import java.time.LocalDateTime;

/* comment.
    MS-2 신고 상세 조회 응답용 DTO. ReportListResponse.Item 과 다르게
    handledAt(처리 시각), handlerAdminId(처리자) 두 필드가 추가되게 된다.
 */

public record ReportDetailResponse(
        Long reportId,
        Long reporterUserId,
        String targetType,
        Long targetId,
        String reason,
        String detail,
        boolean isRead,
        LocalDateTime reportedAt,
        LocalDateTime handledAt
) {

    public static ReportDetailResponse from(Report report) {
        return new ReportDetailResponse(
                report.getId(),
                report.getReporterUserId(),
                report.getTargetType().name(),
                report.getTargetId(),
                report.getReason().name(),
                report.getDetail(),
                report.isResolved(),
                report.getCreatedAt(),
                report.getResolvedAt()
        );
    }

}
