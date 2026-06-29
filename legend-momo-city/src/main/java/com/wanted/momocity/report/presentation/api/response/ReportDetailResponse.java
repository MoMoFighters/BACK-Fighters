package com.wanted.momocity.report.presentation.api.response;

import com.wanted.momocity.report.domain.model.Report;
import com.wanted.momocity.report.application.usecase.ReportQueryUseCase;

import java.time.LocalDateTime;

/* comment.
    MS-2 신고 상세 조회 응답용 DTO. ReportListResponse.Item 과 다르게
    resolved(처리 시각) 이 추가된 형태이다.
 */

public record ReportDetailResponse(
        Long reportId,
        Long reporterUserId,
        String reporterName,
        Long reportedUserId,
        String reportedName,
        String targetType,
        Long parentId,
        Long targetId,
        String targetPath,
        String targetContent,
        String reason,
        String detail,
        boolean isResolved,
        LocalDateTime createdAt,
        LocalDateTime resolvedAt
) {

    // ReportDetail (report + 이름과 내용)을 받아서 응답 DTO 로 반환
    public static ReportDetailResponse from(ReportQueryUseCase.ReportDetail detail) {
        Report report = detail.report();
        return new ReportDetailResponse(
                report.getId(),
                report.getReporterUserId(),
                detail.reporterName(),
                report.getReportedUserId(),
                detail.reportedName(),
                report.getTargetType().name(),
                detail.parentId(),
                report.getTargetId(),
                report.getTargetPath(),
                detail.targetContent(),
                report.getReason().toKorean(),
                report.getDetail(),
                report.isResolved(),
                report.getCreatedAt(),
                report.getResolvedAt()
        );
    }

}
