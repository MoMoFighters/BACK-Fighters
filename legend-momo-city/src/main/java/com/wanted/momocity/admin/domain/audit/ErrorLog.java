package com.wanted.momocity.admin.domain.audit;

import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;

import java.time.LocalDateTime;

/* comment.
    ErrorLog 정리
    1. 이 클래스의 역할 :
    2. 위치 : admin/domain/audit (도메인 계층 - 감사/모니터링)
    3. 왜 모든 필드가 final 인가 (Member 와 다른 점) :
    4. 필드 5개 의미 :
        - id          : 식별자 (DB auto-increment, 신규 발생 시 null)
        - level       : 심각도 (CRITICAL / ERROR / WARNING)
        - source      : 에러 출처 (예: "API Error", "Database", "Frontend", "Server")
        - message     : 에러 메시지 (예: "Payment gateway timeout")
        - occurredAt  : 발생 시각
    5. 정적 팩토리 2개의 의도 :
        - occur()   : 신규 에러 발생 시 (id=null, occurredAt=now)
        - restore() : DB 에서 복원 시 (모든 필드 채움)
 */
public class ErrorLog {

    private final Long id;
    private final ErrorLevel level;
    private final String source;
    private final String message;
    private final LocalDateTime occurredAt;

    /* comment.
        private 생성자 + 검증 = DDD 의 "항상 유효한 객체" 패턴
        - 잘못된 데이터로는 ErrorLog 자체가 생성 불가
        - 외부에서 호출 차단 (occur / restore 통해서만)
     */
    private ErrorLog(Long id, ErrorLevel level, String source, String message, LocalDateTime occurredAt) {
        if (level == null) {
            throw new DomainRuleViolationException("에러 레벨은 필수입니다.");
        }
        if (source == null || source.isBlank()) {
            throw new DomainRuleViolationException("에러 출처는 필수입니다.");
        }
        if (message == null || message.isBlank()) {
            throw new DomainRuleViolationException("에러 메시지는 필수입니다.");
        }
        if (occurredAt == null) {
            throw new DomainRuleViolationException("발생 시각은 필수입니다.");
        }
        this.id = id;
        this.level = level;
        this.source = source;
        this.message = message;
        this.occurredAt = occurredAt;
    }

    /* comment.
        신규 에러 발생 시 호출 (시스템이 에러 발생을 알아챘을 때)
        id 는 DB 저장 시 자동 부여 → null
        발생 시각은 호출 시점 (LocalDateTime.now())
     */
    public static ErrorLog occur(ErrorLevel level, String source, String message) {
        return new ErrorLog(null, level, source, message, LocalDateTime.now());
    }

    // DB 에서 읽어온 값으로 기존 객체 복원
    public static ErrorLog restore(Long id, ErrorLevel level, String source, String message, LocalDateTime occurredAt) {
        return new ErrorLog(id, level, source, message, occurredAt);
    }

    // === Getters (Setter 없음 = 불변) ===
    public Long getId() { return id; }
    public ErrorLevel getLevel() { return level; }
    public String getSource() { return source; }
    public String getMessage() { return message; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
}