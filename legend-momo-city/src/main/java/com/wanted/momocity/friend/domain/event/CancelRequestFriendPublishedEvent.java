package com.wanted.momocity.friend.domain.event;

public record CancelRequestFriendPublishedEvent(
        Long fromUserId, //요청 시 들어간 요청자 아이디
        Long toUserId
) {
}
