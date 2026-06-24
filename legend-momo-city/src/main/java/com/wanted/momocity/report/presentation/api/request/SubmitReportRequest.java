package com.wanted.momocity.report.presentation.api.request;

import com.wanted.momocity.report.application.command.SubmitReportCommand;
import com.wanted.momocity.report.domain.model.ReportReason;
import com.wanted.momocity.report.domain.model.ReportTargetType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/* comment.
    SubmitReportRequest 정리
    신고 접수 HTTP 요청 본문 매핑 - toCommand()로 SubmitReportCommand 로 변환해 UseCase 에 전달
 */
@Schema(description = "신고 접수 요청")
public record SubmitReportRequest(

        @Schema(description = "신고 대상 종류", example = "REVIEW")
        @NotNull(message = "신고 대상 종류는 필수입니다.")
        ReportTargetType targetType,

        @Schema(description = "신고 대상 ID (PAGE 타입일 때만 null 허용)", example = "42")
        Long targetId,

        @Schema(description = "신고당한 유저 ID (nullable)", example = "15")
        Long reportedUserId,

        @Schema(description = "신고 대상 URL (nullable)", example = "/lectures/42")
        String targetPath,

        @Schema(description = "신고 사유", example = "SPAM")
        @NotNull(message = "신고 사유는 필수입니다.")
        ReportReason reason,

        @Schema(description = "자유 설명 (최대 1000자)", example = "광고성 메시지를 반복 게시함")
        @Size(max = 1000, message = "자유 설명은 최대 1000자까지 가능합니다.")
        String detail
) {

    // HTTP 요청 → Command 변환 (reporterUserId는 SecurityContext에서 추출해 파라미터로 받음)
    public SubmitReportCommand toCommand(Long reporterUserId) {
        return new SubmitReportCommand(
                reporterUserId,
                targetType,
                targetId,
                reportedUserId,
                targetPath,
                reason,
                detail
        );
    }
}
