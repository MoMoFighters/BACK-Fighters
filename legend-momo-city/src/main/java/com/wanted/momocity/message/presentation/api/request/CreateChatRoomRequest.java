package com.wanted.momocity.message.presentation.api.request;

import java.util.List;

public record CreateChatRoomRequest(
        List<Long> chatMember
) {
}
