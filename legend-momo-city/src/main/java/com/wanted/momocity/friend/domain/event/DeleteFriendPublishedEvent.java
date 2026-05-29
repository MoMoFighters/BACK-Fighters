package com.wanted.momocity.friend.domain.event;

public record DeleteFriendPublishedEvent(
        //친구 삭제한 장본인
        Long loginUserId,
        //삭제 당한 사람
        Long targetUserId
) {
}
