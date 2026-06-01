package com.wanted.momocity.user.presentation.api.response;

import java.time.Instant;
import java.time.LocalDateTime;

/* comment.
    TeacherActionResponse 정리
    1. 해당 클래스가 하는 일: 강사 승인/반려의 HTTP 응답 본문 매핑 객체
    2. UseCase 의 TeacherActionResult 와 관계 :
        - TeacherActionResult : 응용 계층 반환 (필드 4개)
        - TeacherActionResponse : 표현 계층 반환 (필드 4개, 같은 모양)
        - Controller 가 ActionResult → ActionResponse 변환 (현재는 거의 1:1)
    3. 4개 필드 :
        - userId : 처리된 신청자 PK
        - status : 처리 결과 ("ACTIVE" 승인 / "REJECTED" 반려)
        - reason : 반려 사유 (승인 시 null)
        - processedAt : 처리 시각 (Instant - UTC)
    4. 승인/반려 *공통* 응답 :
        - status 필드로 분기
        - reason 으로 사유 표현 (있/없 으로 구분)
 */

public record TeacherActionResponse(
        Long userId,
        String status,
        String reason,
        LocalDateTime processedAt
) {
}
