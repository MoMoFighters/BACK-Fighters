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
        - status          : 신고 처리 상태 (PENDING/REVIEWING/CONFIRMED/REJECTED)
        - reportedAt      : 접수 시각
        - handledAt       : 처리 시각 (검토 후, nullable)
        - handlerAdminId  : 처리자 (검토 후, nullable)
    4. 불변/가변 구분 :
        - 불변(final) : id, reporterUserId, targetType, targetId, reason, detail, reportedAt
        - 가변        : status, handledAt, handlerAdminId (review/confirm/reject 로 변경)
    5. 정적 팩토리 2개 의도 :
        - submit()  : 신규 신고 접수 (id=null, status=PENDING, reportedAt=now)
        - restore() : DB 복원 (모든 필드 그대로)
    6. 도메인 행위 3개 (module04 stub) :
        - review()         : PENDING → REVIEWING 전이 (module04)
        - confirm(adminId) : REVIEWING → CONFIRMED 전이 (module04)
        - reject(adminId)  : REVIEWING → REJECTED 전이 (module04)
 */
public class Report {

    // 불변성 선언
    private final Long id;
    private final Long reporterUserId;
    private final ReportTargetType targetType;
    private final Long targetId;
    private final ReportReason reason;
    private final String detail;
    private ReportStatus status;
    private final LocalDateTime reportedAt;
    private LocalDateTime handledAt;
    private Long handlerAdminId;

    /* comment.
        private 생성자 + 검증 = DDD "항상 유효한 객체" 패턴
        외부에서 호출 차단 (submit / restore 통해서만 생성)
     */
    private Report(Long id, Long reporterUserId, ReportTargetType targetType, Long targetId,
                   ReportReason reason, String detail, ReportStatus status,
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
        if (status == null) {
            throw new DomainRuleViolationException("신고 상태는 필수입니다.");
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
        this.status = status;
        this.reportedAt = reportedAt;
        this.handledAt = handledAt;
        this.handlerAdminId = handlerAdminId;
    }

    /**
     * 신규 신고 접수 (사용자가 신고 버튼 누름)
     * - id = null (DB 가 부여)
     * - status = PENDING
     * - reportedAt = now()
     * - handledAt, handlerAdminId = null
     */
    // 신규 신고 접수 - 항상 PENDING 상태로 진행하며 현재 시각으로 시작된다.
    public static Report submit(Long reporterUserId, ReportTargetType targetType, Long targetId,
                                ReportReason reason, String detail) {
        return new Report(
                null, // DB auto-increment
                reporterUserId,
                targetType,
                targetId,
                reason,
                detail, // null 허용
                ReportStatus.PENDING, // 신규는 무조건 PENDING
                LocalDateTime.now(), // 접수 시각 = 호출 시점
                null, // handledAt : 검토 전이라 null
                null // handlerAdminId : 검토 전이라 null
        );
    }

    /**
     * DB 에서 읽어온 값으로 기존 객체 복원
     */
    public static Report restore(Long id, Long reporterUserId, ReportTargetType targetType, Long targetId,
                                 ReportReason reason, String detail, ReportStatus status,
                                 LocalDateTime reportedAt, LocalDateTime handledAt, Long handlerAdminId) {
        return new Report(id, reporterUserId, targetType, targetId, reason, detail, status,
                reportedAt, handledAt, handlerAdminId);
    }

    // === 도메인 행위 (module04 stub) ===

    // PENDING → REVIEWING 전이
    public void review() {
        throw new UnsupportedOperationException("TODO: module04 - 신고 검토 시작");
    }

    // REVIEWING → CONFIRMED 전이 (관리자 인정)
    public void confirm(Long adminId) {
        throw new UnsupportedOperationException("TODO: module04 - 신고 인정");
    }

    // REVIEWING → REJECTED 전이 (관리자 기각)
    public void reject(Long adminId) {
        throw new UnsupportedOperationException("TODO: module04 - 신고 기각");
    }

    // === Getters (Setter 없음 = 도메인 행위로만 변경) ===
    public Long getId() { return id; }
    public Long getReporterUserId() { return reporterUserId; }
    public ReportTargetType getTargetType() { return targetType; }
    public Long getTargetId() { return targetId; }
    public ReportReason getReason() { return reason; }
    public String getDetail() { return detail; }
    public ReportStatus getStatus() { return status; }
    public LocalDateTime getReportedAt() { return reportedAt; }
    public LocalDateTime getHandledAt() { return handledAt; }
    public Long getHandlerAdminId() { return handlerAdminId; }
}