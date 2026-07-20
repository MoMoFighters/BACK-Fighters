package com.wanted.momocity.study.domain.event;

import java.time.LocalDateTime;

/*
 * comment.
 *  그룹방 초대 발송 이벤트 (초대 즉시 발행)
 *  - infrastructure.event 쪽이 아니라 notification 도메인의
 *    NotificationLifecycleEventHandler가 직접 수신 -> notification 테이블 적재
 *  -
 *  inviterNickname을 이벤트 자체에 실어 보냄 (community의 PostLikedEvent 등과 동일한 패턴)
 *  -> 알림 문구 조립에 필요한 정보를 이벤트 발행 시점에 미리 확정해서 넘김
 * */

public record StudyInviteEvent(
        Long roomId,
        Long invitedUserId,
        String inviterNickname,
        LocalDateTime invitedAt
) {
}
