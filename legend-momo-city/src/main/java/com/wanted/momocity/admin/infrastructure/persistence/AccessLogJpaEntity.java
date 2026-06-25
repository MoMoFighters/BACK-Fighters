package com.wanted.momocity.admin.infrastructure.persistence;

/* comment.
    access_log 테이블과 1:1 매핑되는 JPA 엔티티.
    toDomain() 으로 도메인 객체로 변환
 */

import com.wanted.momocity.admin.domain.access.AccessLog;
import com.wanted.momocity.admin.domain.access.AccessLogAction;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
// 해당 JPA 엔티티가 어떤 DB 와 연결될지 저장하는 어노테이션
@Table(name = "access_log")
public class AccessLogJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "ip", length = 45)
    private String ip;

    @Column(name = "action", length = 20)
    private String action;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    protected AccessLogJpaEntity() {}

    public AccessLog toDomain() {
        return AccessLog.restore(
                id,
                userId,
                ip,
                AccessLogAction.valueOf(action),
                createdAt
        );
    }

    // toDomain() 은 JpaEntity 의 필드값을 꺼내서 도메인 객체로 바꿔주는 변환 메서드
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getIp() { return ip; }
    public String getAction() { return action; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}