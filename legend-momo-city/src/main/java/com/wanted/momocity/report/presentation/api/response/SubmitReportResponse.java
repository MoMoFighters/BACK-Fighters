package com.wanted.momocity.report.presentation.api.response;

import com.wanted.momocity.report.domain.model.Report;

import java.time.LocalDateTime;

/* comment.
    SubmitReportResponse 정리
    1. 역할 : 신고 접수 성공 시 HTTP 응답으로 보내는 DTO. Controller 가 도메인 Report 를 이걸로 변화내서 반환한다.
    2. 위치 : 표현 계층 - 출력 DTO
    3. WHY record 사용
       → 불변 객체이기 때문이다
       → enrollment.CreateEnrollmentResponse 와 동일한 패턴
    4. WHY enum 을 String 으로 변환 (targetType, reason, status)
       → 표현 계층은 enum 값을 직접적으로 노출시키지 않는다.
       → JSON 직렬화 시 enum 그대로 노출하면 FE 에서 상수 매핑 부담
    5. WHY from(Report) 정적 팩토리 메서드
       → 변환 로직을 한 곳에 모아 응집도
       → enum.name() 으로 변환해서 문자열로 명확하게 전달한다.
    6. 필드 8개 의미
        reportId : 신고 식별자 (FE 가 추적용)
        reporterUserId : 신고자 ID (검증/확인용)
        targetType : 신고 대상 종류 (POST / COMMENT / USER / LECTURE)
        targetId : 신고 대상 ID
        reason : 신고 사유 (enum → String)
        detail : 자유 설명 (nullable, JSON null 로 전달)
        status : 신고 처리 상태 (접수 직후 = PENDING)
        reportedAt : 신고 접수 시각
 */
public record SubmitReportResponse(
        Long reportId,
        Long reporterUserId,
        String targetType,
        Long targetId,
        String reason,
        String detail,
        String status,
        LocalDateTime reportedAt
) {

    public static SubmitReportResponse from(Report report) {
        return new SubmitReportResponse(
                report.getId(),
                report.getReporterUserId(),
                report.getTargetType().name(),
                report.getTargetId(),
                report.getReason().name(),
                report.getDetail(),
                report.getStatus().name(),
                report.getReportedAt()
        );
    }
}