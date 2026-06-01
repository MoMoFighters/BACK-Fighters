package com.wanted.momocity.member.application.command;

import com.wanted.momocity.member.domain.model.MemberStatus;

/* comment.
    ChangeMemberStatusCommand 정리
    1. 이 record 의 역할 : 관리자가 회원 상태 변경 요청 시 응용 계층에 전달되는 입력
    2. 위치 : member/application/command (응용 계층 - 입력)
    3. 왜 record 인가 : 불변성을 유지하기 때문에 record 사용
    4. 왜 Request DTO 와 분리하는가 : HTTP 관심사와 비즈니스 입력 분리해 계층을 분리하기 위함
    5. 검증의 의미 : 잘못된 입력이 useCase 진입 못하게 1차적으로 방어한다.
 */
public record ChangeMemberStatusCommand(
        Long userId,
        MemberStatus newStatus
) {
    public ChangeMemberStatusCommand {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("userId 는 1 이상이어야 합니다.");
        }
        if (newStatus == null) {
            throw new IllegalArgumentException("newStatus 는 필수입니다.");
        }
    }
}