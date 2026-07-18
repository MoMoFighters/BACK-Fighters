package com.wanted.momocity.chatbot.infrastructure.persistence;

/* comment.
    Spring Data Jpa 가 메서드명 보고 쿼리 자동 생성해주는 인터페이스이다.
    유저의 오늘자 사용량 행 하나를 찾는 용도이다.
 */

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Optional;

public interface ChatbotDailyUsageJpaRepository extends JpaRepository<ChatbotDailyUsageJpaEntity, Long> {

    // 유저의 특정 날짜 사용량 행 조회 — 없으면 오늘 첫 호출이라는 뜻 (Optional.empty())
    Optional<ChatbotDailyUsageJpaEntity> findByUserIdAndUsageDate(Long userId, LocalDate usageDate);

    // @ 실제 @Lock 이 걸리는 지점은 여기, JPA 쿼리 메서드 레벨
    // "ForUpdate" 접미사를 Spring Data가 키워드로 못 알아들어서 @Query로 직접 JPQL 명시함
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM ChatbotDailyUsageJpaEntity e WHERE e.userId = :userId AND e.usageDate = :usageDate")
    Optional<ChatbotDailyUsageJpaEntity> findByUserIdAndUsageDateForUpdate(Long userId, LocalDate usageDate);
}
