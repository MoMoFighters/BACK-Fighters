package com.wanted.momocity.admin.infrastructure.persistence;

import com.wanted.momocity.global.infrastructure.persistence.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/* comment.
    ErrorLogJpaEntity 정리
    1. 역할 : Error_log 테이블과 1대1 맵핑되는 JPA 저장 모델 (시스템 에러 감사 로그이다)
    2. 위치 : 인프라 계층
    3. WHY 변경 메서드 없음 (Report 와 차이)
       → ErrorLog 도메인이 모든 필드 final 완전 불변 상태이기 때문이다.
       → audit 로그 특성 상 한 번 기록되면 절대 변경이 금지된다.
    4. WHY enum → String 매핑
       → level 은 도메인에서 ErrorLevel enum 종류가 (Critical / error / warning) 이다.
       → 여기서는 String 으로 저장되며, Adapter 가 enum.name() <-> Enum.ValueOf() 변환이 된다.
    5. WHY occurredAt 과 createdAt 둘 다 보존
       → occurredAt : 도메인 의도 (실제 Error 발생 시점) - ErrorLog.occur() 에서 now() 로 채우게 된다.
       → createdAt : 인프라 메타 (DB 의 행 생성 시점) - BaseTimeEntity 가 자동으로 생기게 된다.
    6. WHY message 컬럼 길이 1000
       → TEXT 타입을 쓰지 않는 이유는 인덱싱과 조회 효율을 극한까지 끌어올리기 위해서 사용한다.
       →
 */
@Entity
@Table(name = "error_log")
public class ErrorLogJpaEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "level", nullable = false, length = 20)
    private String level;

    @Column(name = "source", nullable = false, length = 50)
    private String source;

    @Column(name = "message", nullable = false, length = 1000)
    private String message;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    protected ErrorLogJpaEntity() {
    }

    public ErrorLogJpaEntity(Long id, String level, String source, String message, LocalDateTime occurredAt) {
        this.id = id;
        this.level = level;
        this.source = source;
        this.message = message;
        this.occurredAt = occurredAt;
    }

    public Long getId() { return id; }
    public String getLevel() { return level; }
    public String getSource() { return source; }
    public String getMessage() { return message; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
}