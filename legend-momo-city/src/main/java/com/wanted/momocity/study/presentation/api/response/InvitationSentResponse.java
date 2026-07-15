package com.wanted.momocity.study.presentation.api.response;

/*
 * comment.
 *  초대 발송 API가 사용하는 응답 DTO
 *  - 사용 API :
 *      POST   /api/v3/study/rooms/{roomId}/members/invitations              (발송)
 * */

public record InvitationSentResponse(
        Long invitationId,
        Long roomId,
        Long inviteeId,
        String status
) {
}