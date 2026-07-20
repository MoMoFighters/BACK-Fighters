package com.wanted.momocity.study.presentation.api.stomp;

import java.util.Map;

/*
 * comment.
 *  study 그룹방 STOMP 브로드캐스트 공통 payload.
 *  모든 이벤트가 이 하나의 포맷으로 직렬화되어 "/sub/study/room/{roomId}" 로 전송됨.
 *  프론트는 type 필드로 분기해서 처리.
 *  data는 이벤트별로 필요한 필드만 담는 유연한 맵 (이벤트마다 필드가 달라서 고정 DTO 대신 Map 사용)
 * */

public record StudyRoomBroadcastMessage(
        String type,
        Long roomId,
        Map<String, Object> data
) {
}
