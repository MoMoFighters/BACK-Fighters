package com.wanted.momocity.fortune.infrastructure.persistence;

import com.wanted.momocity.fortune.domain.model.UserFortuneLog;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_fortune_logs")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserFortuneLogJpaEntity {
    @Id

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // 사용자에게 선택된 운세 ID를 저장
    @Column(name = "fortune_id", nullable = false)
    private Long fortuneId;

    // 사용자가 운세를 뽑은 KST 기준 날짜를 저장
    @Column(name = "drawn_date", nullable = false)
    private LocalDate drawnDate;

    // Entity가 처음 저장될 때 생성 시각을 자동으로 입력
    @CreatedDate

    // 생성 시각은 최초 저장 이후 수정할 수 없게 설정
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static UserFortuneLogJpaEntity fromDomain(UserFortuneLog domain) {
        UserFortuneLogJpaEntity entity = new UserFortuneLogJpaEntity();
        entity.id = domain.getId();
        entity.userId = domain.getUserId();
        entity.fortuneId = domain.getFortuneId();
        entity.drawnDate = domain.getDrawnDate();
        entity.createdAt = domain.getCreatedAt();
        return entity;
    }

    public UserFortuneLog toDomain() {

        return UserFortuneLog.reconstitute(
                id,
                userId,
                fortuneId,
                drawnDate,
                createdAt
        );
    }
}
