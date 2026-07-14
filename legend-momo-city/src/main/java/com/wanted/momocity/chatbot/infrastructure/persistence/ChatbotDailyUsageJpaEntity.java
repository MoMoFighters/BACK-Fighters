package com.wanted.momocity.chatbot.infrastructure.persistence;

import com.wanted.momocity.global.infrastructure.persistence.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDate;

/* comment.
    Chatbot_daily_usage 테이블 한 행을 그대로 맵핑하는 JPA 영속 객체 - 도메인 모델 ChatbotDailyUsage 와는 별개이다.
    DB 저장 전용!
 */
@Getter
@Entity

/* comment.
    uniqueConstraints 를 빼고 그냥 DB 가 막아주겠지라고 생각할 수 있겠지만,
    이 테이블에 어떤 제약이 걸려있는지 알 수 없다. 따라서 안전 장치 + 문서 역할을 위해 사용
 */

@Table(
        // 1. 테이블 이름 매핑
        name = "chatbot_daily_usage",
        // 유니크 제약조건 선언
        uniqueConstraints = @UniqueConstraint(
                name = "uq_chatbot_daily_usage_user_date",
                columnNames = {"user_id", "usage_date"}
        )
)
public class ChatbotDailyUsageJpaEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "usage_date", nullable = false)
    private LocalDate usageDate;

    @Column(name = "call_count", nullable = false)
    private int callCount;

    @Column(name = "token_used")
    private Integer tokenUsed;

    protected ChatbotDailyUsageJpaEntity() {
    }

    public ChatbotDailyUsageJpaEntity(Long userId, LocalDate usageDate) {
        this.userId = userId;
        this.usageDate = usageDate;
        this.callCount = 0;
    }

    // Adapter 전용 — 도메인에서 이미 계산 끝난 값(callCount, tokenUsed)을 그대로 반영만 함, 비즈니스 로직 없음
    public void updateUsage(int callCount, Integer tokenUsed) {
        this.callCount = callCount;
        this.tokenUsed = tokenUsed;
    }

}
