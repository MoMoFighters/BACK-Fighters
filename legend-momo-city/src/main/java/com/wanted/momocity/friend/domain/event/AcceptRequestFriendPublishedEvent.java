package com.wanted.momocity.friend.domain.event;

public record AcceptRequestFriendPublishedEvent(
        Long triggerUserId,
        String acceptorNickname, //수학한 사람의 닉네임(알림 행: "acceptorNIckname과 친구가 되었습니다.")
        Long friendId //알림 추적용
) {
}
