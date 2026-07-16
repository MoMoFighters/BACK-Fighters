package com.wanted.momocity.study.presentation.api.response;

import java.time.LocalDateTime;

/*
 * comment.
 *  초대 발송/취소/수락/거절 4개 API가 공용으로 사용하는 응답 DTO
 *  - 사용 API :
 *      DELETE /api/v3/study/rooms/{roomId}/members/invitations/{id}         (취소)
 * */

public record InvitationCanceledResponse(
        Long invitationId,
        String status
) {
}