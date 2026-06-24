package com.wanted.momocity.report.application.command;

import com.wanted.momocity.report.domain.model.ReportReason;
import com.wanted.momocity.report.domain.model.ReportTargetType;

/* comment.
    SubmitReportCommand 정리
    신고 접수 UseCase 의 입력 묶음 : Controller 가 HTTP 요청을 받아 이걸로 변환해 UseCase 로 넘긴다.
 */
public record SubmitReportCommand(
        Long reporterUserId,   // 신고자 ID (SecurityContext에서 추출)
        ReportTargetType targetType,
        Long targetId,         // PAGE 타입일 때만 null 허용
        Long reportedUserId,   // 신고당한 유저 ID (nullable)
        String targetPath,     // 신고 대상 URL (nullable)
        ReportReason reason,
        String detail          // 자유 설명 (nullable)
) {
}