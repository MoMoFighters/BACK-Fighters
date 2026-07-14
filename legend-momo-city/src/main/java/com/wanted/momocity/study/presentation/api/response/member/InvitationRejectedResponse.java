package com.wanted.momocity.study.presentation.api.response.member;

/*
 * comment.
 *  초대 발송/취소/수락/거절 4개 API가 공용으로 사용하는 응답 DTO
 *  - 사용 API :
 *      POST   /api/v3/study/rooms/{roomId}/members/invitations/reject       (거절)
 * */

public record InvitationRejectedResponse(
        Long invitationId,
        Long roomId,
        String status
) {
}