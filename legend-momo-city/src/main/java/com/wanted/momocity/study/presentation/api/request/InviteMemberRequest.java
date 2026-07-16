package com.wanted.momocity.study.presentation.api.request;

import jakarta.validation.constraints.NotNull;

/*
 * comment.
 *  그룹방 초대 발송 요청 DTO
 *  - 사용 API : POST /api/v3/study/rooms/{roomId}/members/invitations
 * */

public record InviteMemberRequest(
        @NotNull(message = "초대할 사용자를 선택해주세요.")
        Long inviteeId
) {
}