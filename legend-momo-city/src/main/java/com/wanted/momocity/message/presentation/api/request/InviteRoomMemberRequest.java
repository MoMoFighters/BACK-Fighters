package com.wanted.momocity.message.presentation.api.request;

import java.util.List;

public record InviteRoomMemberRequest(
        List<Long> chatMember //초대할 대상자들
) {
}
