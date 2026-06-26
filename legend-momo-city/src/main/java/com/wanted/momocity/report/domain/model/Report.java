package com.wanted.momocity.report.domain.model;

import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;

import java.time.LocalDateTime;

// 신고 한 건의 정보 + 행위를 표현하는 도메인 모델
public class Report {

    private final Long id;
    private final Long reporterUserId;
    private final ReportTargetType targetType;
    private final Long targetId;
    private final Long reportedUserId;
    private final String targetPath;
    private final ReportReason reason;
    private final String detail;
    private boolean isResolved;
    private final LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

    // 외부에서 new Report() 직접 호출 못 하게 막는 역할이다.
    private Report(Long id, Long reporterUserId, ReportTargetType targetType, Long targetId,
                   Long reportedUserId, String targetPath,
                   ReportReason reason, String detail, boolean isResolved,
                   LocalDateTime createdAt, LocalDateTime resolvedAt) {

        if (reporterUserId == null) {
            throw new DomainRuleViolationException("신고자 ID는 필수입니다.");
        }
        if (targetType == null) {
            throw new DomainRuleViolationException("신고 대상 종류는 필수입니다.");
        }
        if (targetType != ReportTargetType.PAGE && targetId == null) {
            throw new DomainRuleViolationException("PAGE 타입이 아닌 경우 신고 대상 ID는 필수입니다.");
        }
        if (reason == null) {
            throw new DomainRuleViolationException("신고 사유는 필수입니다.");
        }
        if (createdAt == null) {
            throw new DomainRuleViolationException("신고 시각은 필수입니다.");
        }

        this.id = id;
        this.reporterUserId = reporterUserId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.reportedUserId = reportedUserId;
        this.targetPath = targetPath;
        this.reason = reason;
        this.detail = detail;
        this.isResolved = isResolved;
        this.createdAt = createdAt;
        this.resolvedAt = resolvedAt;
    }

    // 사용자가 신고 버튼 누를 때 호출하는 생성 메서드이다. id = null / DB 가 부여한다.
    public static Report submit(Long reporterUserId, ReportTargetType targetType, Long targetId,
                                Long reportedUserId, String targetPath,
                                ReportReason reason, String detail) {
        return new Report(
                null,
                reporterUserId,
                targetType,
                targetId,
                reportedUserId,
                targetPath,
                reason,
                detail,
                false,
                LocalDateTime.now(),
                null
        );
    }

    // DB 에서 꺼낸 데이터를 도메인 객체로 되살릴 때 사용. submit() 과 달리 모든 필드를
    // 그대로 받아서 복원함.
    public static Report restore(Long id, Long reporterUserId, ReportTargetType targetType, Long targetId,
                                 Long reportedUserId, String targetPath,
                                 ReportReason reason, String detail, boolean isResolved,
                                 LocalDateTime createdAt, LocalDateTime resolvedAt) {
        return new Report(id, reporterUserId, targetType, targetId,
                reportedUserId, targetPath, reason, detail, isResolved, createdAt, resolvedAt);
    }

    // 신고 처리 완료 (관리자 검토 후 호출)
    public void resolve() {
        if (this.isResolved) return ; // 이미 처리된 건은 무시
        this.isResolved = true;
        this.resolvedAt = LocalDateTime.now();
    }

    // 외부에서 필드 값을 읽을 수 있게 하는 접근자
    public Long getId() { return id; }
    public Long getReporterUserId() { return reporterUserId; }
    public ReportTargetType getTargetType() { return targetType; }
    public Long getTargetId() { return targetId; }
    public Long getReportedUserId() { return reportedUserId; }
    public String getTargetPath() { return targetPath; }
    public ReportReason getReason() { return reason; }
    public String getDetail() { return detail; }
    public boolean isResolved() { return isResolved; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
}
