package com.wanted.momocity.message.presentation.api.response;

import java.time.LocalDateTime;

public record InviteRoomMemberResponse(
        Long roomId,
        String roomTitle,
        Long userId, //초대 주체
        String nickname, //초대 주체
        String role,
        LocalDateTime joinedAt //초대한 시간
) {
}
