package com.wanted.momocity.report.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

// report 테이블과 1:1 매핑되는 JPA 저장 모델
@Entity
@Table(name = "report")
public class ReportJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "reporter_user_id", nullable = false)
    private Long reporterUserId;

    @Column(name = "target_type", nullable = false, length = 20)
    private String targetType;

    // nullable 속성없음
    // 이유는 PAGE 타입일 경우 null 허용이기 때문에 의도적으로 제거
    @Column(name = "target_id")
    private Long targetId;

    // targetId와 동일하게 nullable이다.
    @Column(name = "reported_user_id")
    private Long reportedUserId;

    // targetId 와 동일하게 nullable이다.
    @Column(name = "target_path", length = 500)
    private String targetPath;

    @Column(name = "reason", nullable = false, length = 30)
    private String reason;

    @Column(name = "detail", length = 1000)
    private String detail;

    @Column(name = "is_resolved", nullable = false)
    private boolean isResolved;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    // JPA 가 리플렉션으로 객체를 만들 때 필요한 기본 생성자
    protected ReportJpaEntity() {
    }

    // Report 객체를 이걸로 변환할 때 호출한다.
    public ReportJpaEntity(Long id, Long reporterUserId, String targetType, Long targetId,
                           Long reportedUserId, String targetPath,
                           String reason, String detail, boolean isResolved,
                           LocalDateTime createdAt, LocalDateTime resolvedAt) {
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

    // JPA 변경 감지용 메서드이다.
    // 여기서는 DB 행 상태만 바꾼다.
    public void resolve() {
        this.isResolved = true;
        this.resolvedAt = LocalDateTime.now();
    }

    // Adapter 의 toDomain() 은 아래의 값을 호출해서 엔티티 값을 도메인 객체로 변환한다.
    public Long getId() { return id; }
    public Long getReporterUserId() { return reporterUserId; }
    public String getTargetType() { return targetType; }
    public Long getTargetId() { return targetId; }
    public Long getReportedUserId() { return reportedUserId; }
    public String getTargetPath() { return targetPath; }
    public String getReason() { return reason; }
    public String getDetail() { return detail; }
    public boolean isResolved() { return isResolved; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
}
