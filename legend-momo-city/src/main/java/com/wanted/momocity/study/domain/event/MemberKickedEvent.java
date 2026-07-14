package com.wanted.momocity.study.domain.event;

/*
 * comment.
 *  그룹방 멤버 강퇴 이벤트
 *  - infrastructure.event.StudyBroadcastEventHandler가 수신 -> STOMP로 방 토픽 브로드캐스트
 *  - 강퇴당한 본인 클라이언트도 이 방 토픽을 구독 중이므로 실시간으로 알 수 있다.
 * */
public record MemberKickedEvent(
        Long roomId,
        Long targetUserId,
        Long hostUserId
) {
}