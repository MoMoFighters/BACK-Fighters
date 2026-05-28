package com.wanted.momocity.report.infrastructure.persistence;

import com.wanted.momocity.global.infrastructure.persistence.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/* comment.
    ReportJpaEntity 정리
    1. 역할 : report 테이블과 1:1 매핑되는 JPA 저장 모델
    2. 신고 영역 단독 소유 : 다른 BC 는 이 클래스에 직접 매핑 금지
    3. Report 도메인 모델과의 관계
       - 같은 신고를 표현하지만 완전히 별개 클래스
       - 변환 책임은 ReportRepositoryAdapter 가 가짐
    4. WHY BaseTimeEntity 상속
       → created_at / updated_at 자동 채움 (@CreatedDate / @LastModifiedDate)
       → reportedAt 과는 의미 분리 (도메인 의도 vs DB row 메타)
    5. WHY enum → String 매핑
       → targetType / reason / status 는 도메인에서 enum, 여기선 String
       → Adapter 가 enum.name() ↔ Enum.valueOf() 변환
       → member / lecture 와 동일 정책 (일관성)
 */
@Entity
@Table(name = "report")
public class ReportJpaEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "reporter_user_id", nullable = false)
    private Long reporterUserId;

    @Column(name = "target_type", nullable = false, length = 20)
    private String targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(name = "reason", nullable = false, length = 30)
    private String reason;

    @Column(name = "detail", length = 1000)
    private String detail;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "reported_at", nullable = false)
    private LocalDateTime reportedAt;

    @Column(name = "handled_at")
    private LocalDateTime handledAt;

    @Column(name = "handler_admin_id")
    private Long handlerAdminId;

    // JPA 기본 생성자 (protected) : 외부 직접 인스턴스화 차단, JPA 가 리플렉션으로 사용
    protected ReportJpaEntity() {
    }

    // Adapter 의 toEntity() 가 호출하는 전체 필드 생성자
    public ReportJpaEntity(Long id, Long reporterUserId, String targetType, Long targetId,
                           String reason, String detail, String status,
                           LocalDateTime reportedAt, LocalDateTime handledAt, Long handlerAdminId) {
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

    // 영속화 모델 메서드 (도메인 행위 아님)
    // 도메인 검증/규칙은 Report.review / confirm / reject 가 담당, 여기는 단순 필드 변경만
    public void changeStatus(String status) {
        this.status = status;
    }

    public void markHandled(LocalDateTime handledAt, Long handlerAdminId) {
        this.handledAt = handledAt;
        this.handlerAdminId = handlerAdminId;
    }

    public Long getId() { return id; }
    public Long getReporterUserId() { return reporterUserId; }
    public String getTargetType() { return targetType; }
    public Long getTargetId() { return targetId; }
    public String getReason() { return reason; }
    public String getDetail() { return detail; }
    public String getStatus() { return status; }
    public LocalDateTime getReportedAt() { return reportedAt; }
    public LocalDateTime getHandledAt() { return handledAt; }
    public Long getHandlerAdminId() { return handlerAdminId; }
}