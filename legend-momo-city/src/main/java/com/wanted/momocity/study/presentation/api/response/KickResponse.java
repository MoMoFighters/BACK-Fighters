package com.wanted.momocity.study.presentation.api.response;

/*
 * comment.
 *  그룹방 멤버 강퇴 응답 DTO
 *  - 사용 API : POST /api/v3/study/rooms/{roomId}/members/{targetUserId}/kick
 *  - 방장만 호출 가능 (검증은 MemberCommandService.kick()이 담당)
 *  - status는 항상 "KICKED" 고정값
 *  - 강퇴된 유저는 이후 이 방으로부터 재초대 자체가 서버에서 차단된(초대 발송 시 KICKED 이력 검증).
 * */

public record KickResponse(
        Long roomId,
        Long targetUserId,
        String status
) {
}