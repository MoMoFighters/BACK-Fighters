package com.wanted.momocity.user.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/* comment.
        TeacherActionRequest 정리
        1. 해당 클래스가 하는 일 : 강사 승인/반려의 HTTPO 요청 본문 매핑 객체
        2. 위치 : teacher/presentation/api/request
        3. Request vs Command 차이 :
                - Request (이 클래스): HTTP 본문 매핑. 표현 계층. Spring Validation 어노테이션
                - Command (ApproveTeacherCommand/RejectTeacherCommand): 응용 계층. HTTP 모름
                - Controller 가 Request → Command 변환
        4. 필드 2개 :
                - action : "APPROVE" 또는 "REJECT" (필수)
                - reason : 반려 사유 (REJECT 시 필수, APPROVE 시 무시)
         5. 검증의 두 단계 :
                - 1단계 (이 파일) : *HTTP 형식 검증* - action 필수, 값 형식 일치
                - 2단계 (Service) : *비즈니스 검증* - reason 10자 이상 (REJECT 시)
 */

public record TeacherActionRequest(

        @Schema(description = "강사 승인 또는 반려", example = "APPROVE")
        @NotBlank(message = "action 은 필수입니다")
        @Pattern(regexp = "APPROVE|REJECT", message = "action 값은 APPROVE 또는 REJECT 여야 합니다")
        String action,

        @Schema(description = "반려 사유 (REJECT 시 필수, 최소 10자)", example = "자격증 서류가 불충분합니다.",minLength = 10)
        String reason
) {
}
