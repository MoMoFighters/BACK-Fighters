package com.wanted.momocity.friend.domain.event;

public record AcceptRequestFriendPublishedEvent(
        Long acceptorUserId, //수락한 사람
        String acceptorNickname, //수락한 사람의 닉네임(알림 행: "acceptorNIckname과 친구가 되었습니다.")
        Long fromUserId //refId(먼저 요청 보낸 사람)
) {
}
