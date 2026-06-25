package com.wanted.momocity.notification.presentation.api.request;

import java.util.List;

public record CreateChatRoomRequest(
        List<Long> chatMember
) {
}
