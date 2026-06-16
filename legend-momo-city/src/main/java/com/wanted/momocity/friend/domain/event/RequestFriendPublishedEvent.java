package com.wanted.momocity.friend.domain.event;

public record RequestFriendPublishedEvent(
        Long fromUserId, //notification 테이블의 refId
        String fromUserNickname,
        Long toUserId //notification 테이블의 userId
) {
}
