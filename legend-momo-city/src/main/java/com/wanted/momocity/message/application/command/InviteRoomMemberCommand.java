package com.wanted.momocity.message.application.command;

import java.util.List;

public record InviteRoomMemberCommand(
        Long roomId,
        Long userId,
        List<Long> chatMember //초대할 대상자들
) {
}
