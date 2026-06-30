package com.wanted.momocity.friend.domain.event;

public record RejectRequestFriendPublishedEvent(
        Long userId, //거절자
        Long fromUserId,
        Long refId
) {
}
