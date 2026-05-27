package com.wanted.momocity.admin;

import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.momocity.report.domain.model.Report;
import com.wanted.momocity.report.domain.model.ReportReason;
import com.wanted.momocity.report.domain.model.ReportStatus;
import com.wanted.momocity.report.domain.model.ReportTargetType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/* comment.
    ReportTest - TDD 시작점
    1. 이 테스트 클래스의 역할 :
    2. TDD 의 의도 (red → green → refactor) :
    3. 컴파일 실패가 정상인 이유 :
    4. @Nested 그룹화 (Submit / Restore) :
    5. ErrorLogTest 와 같은 패턴 사용 :
 */
@DisplayName("Report 도메인 모델")
class ReportTest {

    @Nested
    @DisplayName("submit() 정적 팩토리 - 신규 신고 접수")
    class Submit {

        @Test
        @DisplayName("정상 생성 - id=null, status=PENDING, reportedAt=현재시각")
        void submit_정상_생성_성공() {
            // when
            Report report = Report.submit(
                    10L,                            // reporterUserId
                    ReportTargetType.LECTURE,       // targetType
                    100L,                           // targetId
                    ReportReason.INAPPROPRIATE,     // reason
                    "강의 내용이 부적절합니다"      // detail
            );

            // then
            assertThat(report.getId()).isNull();
            assertThat(report.getReporterUserId()).isEqualTo(10L);
            assertThat(report.getTargetType()).isEqualTo(ReportTargetType.LECTURE);
            assertThat(report.getTargetId()).isEqualTo(100L);
            assertThat(report.getReason()).isEqualTo(ReportReason.INAPPROPRIATE);
            assertThat(report.getDetail()).isEqualTo("강의 내용이 부적절합니다");
            assertThat(report.getStatus()).isEqualTo(ReportStatus.PENDING);
            assertThat(report.getReportedAt()).isNotNull();
            assertThat(report.getReportedAt()).isBeforeOrEqualTo(LocalDateTime.now());
            assertThat(report.getHandledAt()).isNull();
            assertThat(report.getHandlerAdminId()).isNull();
        }

        @Test
        @DisplayName("detail 없이도 신고 가능 - detail=null 허용")
        void submit_detail_null_허용() {
            Report report = Report.submit(
                    10L,
                    ReportTargetType.USER,
                    7L,
                    ReportReason.SPAM,
                    null
            );

            assertThat(report.getDetail()).isNull();
            assertThat(report.getStatus()).isEqualTo(ReportStatus.PENDING);
        }

        @Test
        @DisplayName("reporterUserId null - DomainRuleViolationException")
        void submit_reporterUserId_null_예외() {
            assertThatThrownBy(() ->
                    Report.submit(null, ReportTargetType.USER, 7L, ReportReason.SPAM, null)
            )
                    .isInstanceOf(DomainRuleViolationException.class)
                    .hasMessageContaining("신고자");
        }

        @Test
        @DisplayName("targetType null - DomainRuleViolationException")
        void submit_targetType_null_예외() {
            assertThatThrownBy(() ->
                    Report.submit(10L, null, 7L, ReportReason.SPAM, null)
            )
                    .isInstanceOf(DomainRuleViolationException.class)
                    .hasMessageContaining("대상");
        }

        @Test
        @DisplayName("reason null - DomainRuleViolationException")
        void submit_reason_null_예외() {
            assertThatThrownBy(() ->
                    Report.submit(10L, ReportTargetType.USER, 7L, null, null)
            )
                    .isInstanceOf(DomainRuleViolationException.class)
                    .hasMessageContaining("사유");
        }
    }

    @Nested
    @DisplayName("restore() 정적 팩토리 - DB 복원")
    class Restore {

        @Test
        @DisplayName("DB 값 복원 - 모든 필드 채움 (검토 완료 상태)")
        void restore_DB_값_복원_성공() {
            // given
            LocalDateTime reportedAt = LocalDateTime.of(2026, 5, 1, 10, 0);
            LocalDateTime handledAt = LocalDateTime.of(2026, 5, 2, 14, 0);

            // when
            Report report = Report.restore(
                    42L,
                    10L,
                    ReportTargetType.LECTURE,
                    100L,
                    ReportReason.INAPPROPRIATE,
                    "부적절한 내용",
                    ReportStatus.CONFIRMED,
                    reportedAt,
                    handledAt,
                    99L
            );

            // then
            assertThat(report.getId()).isEqualTo(42L);
            assertThat(report.getStatus()).isEqualTo(ReportStatus.CONFIRMED);
            assertThat(report.getHandledAt()).isEqualTo(handledAt);
            assertThat(report.getHandlerAdminId()).isEqualTo(99L);
        }
    }
}
