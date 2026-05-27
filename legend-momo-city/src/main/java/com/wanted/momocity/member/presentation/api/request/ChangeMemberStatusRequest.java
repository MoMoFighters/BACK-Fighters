package com.wanted.momocity.member.presentation.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/* comment.
    ChangeMemberStatusRequest 정리
    1. 이 record 의 역할 : HTTP PATCH 요청 body 를 받는 입력 DTO.JSON -> 자바 객체 변환한다.
    2. 위치 : member/presentation/api/request (표현 계층 - 입력 DTO)
    3. 왜 Command 와 분리하는가 : HTTP 관심사와 비즈니스 관심사 격리해서 계층 책임을 분리한다.
    4. 왜 String 으로 받고 MemberStatus enum 으로 직접 안 받는가 : 검증 후 안전하게 enum 변환, 잘못된 값이 컨틀롤러 진입 차단.
    5. 왜 검증 어노테이션을 여기서 거는가 (@NotBlank, @Pattern) : @Valid 와 함께 작동. 1차 방어선 역할을 한다.
 */
public record ChangeMemberStatusRequest(

        @NotBlank(message = "status 는 필수입니다.")
        @Pattern(
                regexp = "ACTIVE|PENDING|REJECTED|BANNED|BLACK|DELETED",
                message = "status 는 ACTIVE / PENDING / REJECTED / BANNED / BLACK / DELETED 중 하나여야 합니다."
        )
        String status
) { }