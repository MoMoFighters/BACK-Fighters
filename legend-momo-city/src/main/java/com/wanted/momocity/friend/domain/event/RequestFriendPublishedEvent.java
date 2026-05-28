package com.wanted.momocity.friend.domain.event;

public record RequestFriendPublishedEvent(
        Long fromUserId,
        String fromUserNickname,
        Long toUserId,
        Long friendId //알림 테이블의 ref_id로 매핑될 고유 ID
) {
}
