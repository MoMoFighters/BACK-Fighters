package com.wanted.momocity.study.presentation.api.response.member;

import java.time.LocalDateTime;
import java.util.List;

/*
 * comment.
 *  내가 보낸 초대 목록 응답 DTO
 *  - 사용 API : GET /api/v3/study/members/invitations/sent
 * */

public record SentInvitationListResponse(
        List<SentInvitationItem> invitations
) {
    public record SentInvitationItem(
            Long invitationId,
            Long roomId,
            String title,
            Long inviteeId,
            String inviteeNickname,
            String inviteeProfileImageUrl,
            LocalDateTime invitedAt
    ) {}
}
