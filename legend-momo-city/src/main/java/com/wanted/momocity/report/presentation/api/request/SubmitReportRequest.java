package com.wanted.momocity.report.presentation.api.request;

import com.wanted.momocity.report.application.command.SubmitReportCommand;
import com.wanted.momocity.report.domain.model.ReportReason;
import com.wanted.momocity.report.domain.model.ReportTargetType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/* comment.
    SubmitReportRequest 정리
    1. 역할 : 산고 접수 HTTP 요청 본문 매핑 객체
    2. 위치 : 표현 계층 - HTTP 입력 DTO
    3. Request vs Command 차이
        Request : HTTP 본문 매핑. 표현 계층. Spring Validation 어노테이션 사용.
        Command : 응용 계층 의도. HTTP 모름. 도메인이 알아야 할 정보만 담음.
        변환 책임 : Controller (Request → Command). 이 파일의 toCommand() 메서드가 변환 담당.
    4. 필드 4개 (reporterEmail 은 왜 없는가)
        targetType : 신고 대상 종류 enum (POST / COMMENT / USER / LECTURE)
        targetId : 신고 대상 ID (외부 BC 참조)
        reason : 신고 사유 enum (SPAM / ABUSE / ...)
        detail : 자유 설명 (nullable, 최대 1000자)
       → WHY reporterEmail 없음 : client 가 보내는 것이 아니라 서버가 Authentication 에서 추출하기 때문에
       또한 client 입력 절대 신뢰할 수 없다. (도용당한 계정일 수 있기 때문이다)
    5. 검증의 두 단계
       - 1단계 (이 파일) : HTTP 형식 검증 - 필수값/양수/길이제한
       - 2단계 (Service) : 비즈니스 검증 - 본인 신고 방지, 중복 신고 검증 등
    6. WHY toCommand(email) 메서드 추가
       → 변환 로직을 Request DTO 안에 응집되게 된다. Controller 검증
       → from() 응답 DTO 과 대칭되는 네이밍
 */
@Schema(description = "신고 접수 요청")
public record SubmitReportRequest(

        @Schema(description = "신고 대상 종류", example = "USER")
        @NotNull(message = "신고 대상 종류는 필수입니다.")
        ReportTargetType targetType,

        @Schema(description = "신고 대상 ID", example = "42")
        @NotNull(message = "신고 대상 ID 는 필수입니다.")
        @Positive(message = "신고 대상 ID 는 양수여야 합니다.")
        Long targetId,

        @Schema(description = "신고 사유", example = "SPAM")
        @NotNull(message = "신고 사유는 필수입니다.")
        ReportReason reason,

        @Schema(description = "자유 설명 (최대 1000자)", example = "광고성 메시지를 반복 게시함")
        @Size(max = 1000, message = "자유 설명은 최대 1000자까지 가능합니다.")
        String detail
) {

    public SubmitReportCommand toCommand(Long reporterUserId) {
        return new SubmitReportCommand(
                reporterUserId,
                targetType,
                targetId,
                reason,
                detail
        );
    }
}