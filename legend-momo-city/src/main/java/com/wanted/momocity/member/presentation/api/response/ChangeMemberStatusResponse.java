package com.wanted.momocity.member.presentation.api.response;

import java.time.LocalDateTime;

/* comment.
    ChangeMemberStatusResponse 정리
    1. 이 record 의 역할 : 상태 변경 결과를 클라이언트로 돌려주는 HTTP 응답 body
    2. 위치 : member/presentation/api/response (표현 계층 - 출력 DTO)
    3. 왜 Result(MemberStatusChangeResult) 와 분리하는가 : 응용 Result와 HTTP Response 격리 목적
    4. 왜 필드가 응용 Result 와 동일한가 : 응용 출력을 그대로 클라이언트에 전달해준다.
    5. Swagger 어노테이션(@Schema) 이 지금 왜 없는가 : 현재는 골격만 진행
 */
public record ChangeMemberStatusResponse(
        Long userId,
        String status,
        LocalDateTime changedAt
) { }