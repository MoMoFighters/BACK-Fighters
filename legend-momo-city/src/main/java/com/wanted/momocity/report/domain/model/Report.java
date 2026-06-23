package com.wanted.momocity.report.domain.model;

import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;

import java.time.LocalDateTime;

/* comment.
    Report 도메인 모델 정리
    1. 이 클래스의 역할 : 신고 한 건의 정보 + 행위를 표현하는 도메인 모델
    2. 위치 : report/domain/model (도메인 계층)
    3. 10 필드 의미 :
        - id              : 식별자 (DB auto-increment, 신규 시 null)
        - reporterUserId  : 신고자 (외부 BC 회원의 user.id 참조)
        - targetType      : 신고 대상 종류 (POST/COMMENT/USER/LECTURE)
        - targetId        : 신고 대상 ID (외부 참조, BC 경계 침범 X)
        - reason          : 신고 사유 (SPAM/ABUSE/...)
        - detail          : 자유 설명 (nullable)
        - isRead          : 읽음 여부 (false=미읽음, true=읽음)
        - reportedAt      : 접수 시각
        - handledAt       : 처리 시각 (검토 후, nullable)
        - handlerAdminId  : 처리자 (검토 후, nullable)
    4. 불변/가변 구분 :
        - 불변(final) : id, reporterUserId, targetType, targetId, reason, detail, reportedAt
        - 가변        : isRead, handledAt, handlerAdminId (markAsRead 로 변경)
    5. 정적 팩토리 2개 의도 :
        - submit()  : 신규 신고 접수 (id=null, isRead=false, reportedAt=now)
        - restore() : DB 복원 (모든 필드 그대로)
    6. 도메인 행위 :
        - markAsRead() : 신고를 읽음 처리 (isRead = true)
 */
public class Report {

    // 불변성 선언
    private final Long id;
    private final Long reporterUserId;
    private final ReportTargetType targetType;
    private final Long targetId;
    private final ReportReason reason;
    private final String detail;
    private boolean isRead;
    private final LocalDateTime reportedAt;
    private LocalDateTime handledAt;
    private Long handlerAdminId;

    /* comment.
        private 생성자 + 검증 = DDD "항상 유효한 객체" 패턴
        외부에서 호출 차단 (submit / restore 통해서만 생성)
     */
    private Report(Long id, Long reporterUserId, ReportTargetType targetType, Long targetId,
                   ReportReason reason, String detail, boolean isRead,
                   LocalDateTime reportedAt, LocalDateTime handledAt, Long handlerAdminId) {

        if (reporterUserId == null) {
            throw new DomainRuleViolationException("신고자 ID 는 필수입니다.");
        }
        if (targetType == null) {
            throw new DomainRuleViolationException("신고 대상 종류는 필수입니다.");
        }
        if (reason == null) {
            throw new DomainRuleViolationException("신고 사유는 필수입니다.");
        }
        if (reportedAt == null) {
            throw new DomainRuleViolationException("신고 시각은 필수입니다.");
        }

        this.id = id;
        this.reporterUserId = reporterUserId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.reason = reason;
        this.detail = detail;
        this.isRead = isRead;
        this.reportedAt = reportedAt;
        this.handledAt = handledAt;
        this.handlerAdminId = handlerAdminId;
    }

    /**
     * 신규 신고 접수 (사용자가 신고 버튼 누름)
     * - id = null (DB 가 부여)
     * - isRead = false (신규 신고는 미읽음 상태로 시작)
     * - reportedAt = now()
     * - handledAt, handlerAdminId = null
     */
    // 신규 신고 접수 - isRead=false 로 시작하며 현재 시각으로 접수된다.
    public static Report submit(Long reporterUserId, ReportTargetType targetType, Long targetId,
                                ReportReason reason, String detail) {
        return new Report(
                null, // DB auto-increment
                reporterUserId,
                targetType,
                targetId,
                reason,
                detail, // null 허용
                false,
                LocalDateTime.now(), // 접수 시각 = 호출 시점
                null, // handledAt : 검토 전이라 null
                null // handlerAdminId : 검토 전이라 null
        );
    }

    /**
     * DB 에서 읽어온 값으로 기존 객체 복원
     */
    public static Report restore(Long id, Long reporterUserId, ReportTargetType targetType, Long targetId,
                                 ReportReason reason, String detail, boolean isRead,
                                 LocalDateTime reportedAt, LocalDateTime handledAt, Long handlerAdminId) {
        return new Report(id, reporterUserId, targetType, targetId, reason, detail, isRead,
                reportedAt, handledAt, handlerAdminId);
    }

    public void markAsRead() {
        this.isRead = true;
    }

    // 관리자가 이 신고를 검토하고 처리를 완료했다 (도메인 행위)
    public void resolve(Long adminId) {
        this.isRead = true;
        this.handledAt = LocalDateTime.now();
        this.handlerAdminId = adminId;
    }

    // === Getters (Setter 없음 = 도메인 행위로만 변경) ===
    public Long getId() { return id; }
    public Long getReporterUserId() { return reporterUserId; }
    public ReportTargetType getTargetType() { return targetType; }
    public Long getTargetId() { return targetId; }
    public ReportReason getReason() { return reason; }
    public String getDetail() { return detail; }
    public boolean isRead() { return isRead; }
    public LocalDateTime getReportedAt() { return reportedAt; }
    public LocalDateTime getHandledAt() { return handledAt; }
    public Long getHandlerAdminId() { return handlerAdminId; }
}