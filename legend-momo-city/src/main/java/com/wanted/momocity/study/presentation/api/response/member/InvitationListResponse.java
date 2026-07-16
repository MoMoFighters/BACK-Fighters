package com.wanted.momocity.study.presentation.api.response.member;

import java.time.LocalDateTime;
import java.util.List;

/*
 * comment.
 *  내가 받은 초대 목록 응답 DTO
 *  - 사용 API : GET /api/v3/study/members/invitations
 *  - roomId를 거치지 않는 독립 경로 - 본인이 어느 방에서 초대받았는지 전체를 한 번에 조회
 *  - status=INVITED인 건만 포함 (수락/거절/취소된 건은 제외)
 *  - hostUserId를 포함하는 이유 : 추후 프로필 이동/메시지 보내기 등 액션 확장 대비
 * */


public record InvitationListResponse(
        List<InvitationItem> invitations
) {
    public record InvitationItem(
            Long invitationId,
            Long roomId,
            String title,
            Long hostUserId,
            String hostNickname,
            LocalDateTime invitedAt
    ) {}
}