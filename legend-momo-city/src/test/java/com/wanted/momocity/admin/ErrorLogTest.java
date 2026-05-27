package com.wanted.momocity.admin;

import com.wanted.momocity.admin.domain.audit.ErrorLevel;
import com.wanted.momocity.admin.domain.audit.ErrorLog;
import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/* comment.
    ErrorLogTest - DDD 도메인 모델 단위 테스트
    1. 이 테스트 클래스의 역할 : ErrorLog 도메인 모델의 행위를 검증하는 단위 테스트
    2. 위치 : src/test/java/.../admin/domain/audit (main/ 의 4계층 미러링)
    3. @Nested 란? : 테스트 클래스 안에서 의도별로 트리 만드는 그룹화 도구
    4. 왜 외부 의존 0% 인가 (Spring/DB 없음) : 도메인 모델은 Spring/DB 에 대해서 모른다.
    따라서 Spring 없이 순수 자바 객체로 검증이 가능하다.
    5. given/when/then 패턴 의도 : 어떤걸로, 뭘 하면, 어떻게 결과가 나오는지 분리
 */
@DisplayName("ErrorLog 도메인 모델")
class ErrorLogTest {

    @Nested
    @DisplayName("occur() 정적 팩토리 - 신규 에러 발생")
    class Occur {

        @Test
        @DisplayName("정상 생성 - id=null, occurredAt=현재시각")
        void occur_정상_생성_성공() {
            // when
            ErrorLog errorLog = ErrorLog.occur(
                    ErrorLevel.CRITICAL,
                    "API Error",
                    "Payment gateway timeout"
            );

            // then
            assertThat(errorLog.getId()).isNull();
            assertThat(errorLog.getLevel()).isEqualTo(ErrorLevel.CRITICAL);
            assertThat(errorLog.getSource()).isEqualTo("API Error");
            assertThat(errorLog.getMessage()).isEqualTo("Payment gateway timeout");
            assertThat(errorLog.getOccurredAt()).isNotNull();
            assertThat(errorLog.getOccurredAt()).isBeforeOrEqualTo(LocalDateTime.now());
        }

        @Test
        @DisplayName("level null - DomainRuleViolationException")
        void occur_level_null_예외() {
            assertThatThrownBy(() ->
                    ErrorLog.occur(null, "API Error", "msg")
            )
                    .isInstanceOf(DomainRuleViolationException.class)
                    .hasMessageContaining("에러 레벨은 필수");
        }

        @Test
        @DisplayName("source 빈 문자열 - DomainRuleViolationException")
        void occur_source_빈문자열_예외() {
            assertThatThrownBy(() ->
                    ErrorLog.occur(ErrorLevel.ERROR, "", "msg")
            )
                    .isInstanceOf(DomainRuleViolationException.class)
                    .hasMessageContaining("에러 출처는 필수");
        }

        @Test
        @DisplayName("message 빈 문자열 - DomainRuleViolationException")
        void occur_message_빈문자열_예외() {
            assertThatThrownBy(() ->
                    ErrorLog.occur(ErrorLevel.ERROR, "API Error", "")
            )
                    .isInstanceOf(DomainRuleViolationException.class)
                    .hasMessageContaining("에러 메시지는 필수");
        }
    }

    @Nested
    @DisplayName("restore() 정적 팩토리 - DB 복원")
    class Restore {

        @Test
        @DisplayName("DB 값 복원 - 모든 필드 채움")
        void restore_DB_값_복원_성공() {
            // given
            LocalDateTime past = LocalDateTime.of(2026, 5, 1, 10, 0);

            // when
            ErrorLog errorLog = ErrorLog.restore(
                    42L,
                    ErrorLevel.WARNING,
                    "Database",
                    "Connection pool exhausted",
                    past
            );

            // then
            assertThat(errorLog.getId()).isEqualTo(42L);
            assertThat(errorLog.getLevel()).isEqualTo(ErrorLevel.WARNING);
            assertThat(errorLog.getSource()).isEqualTo("Database");
            assertThat(errorLog.getMessage()).isEqualTo("Connection pool exhausted");
            assertThat(errorLog.getOccurredAt()).isEqualTo(past);
        }

        @Test
        @DisplayName("occurredAt null - DomainRuleViolationException")
        void restore_occurredAt_null_예외() {
            assertThatThrownBy(() ->
                    ErrorLog.restore(1L, ErrorLevel.ERROR, "API", "msg", null)
            )
                    .isInstanceOf(DomainRuleViolationException.class)
                    .hasMessageContaining("발생 시각은 필수");
        }
    }
}