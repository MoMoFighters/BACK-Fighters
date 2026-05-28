package com.wanted.momocity.friend.domain.event;

public record CancelRequestFriendPublishedEvent(
        Long friendId //요청 시 들어간 friend 테이블의 행 ID
) {
}
