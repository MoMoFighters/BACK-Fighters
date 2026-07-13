package com.wanted.momocity.notification.presentation.api.response;

public record GetPhoneAppCountsResponse(
        Long totalMsgFriendCount, //친구+메시지
        Long calendarCount, //캘린더
        Long communityCount, //커뮤니티(게시글, 댓글, 대댓글)
        Long studyCount //열품타 초대 알림
) {
}
