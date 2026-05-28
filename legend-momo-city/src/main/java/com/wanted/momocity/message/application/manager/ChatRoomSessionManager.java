package com.wanted.momocity.message.application.manager;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatRoomSessionManager {

    //key: 유저ID, value: 현재 머물고 있는 방ID(방에서 나가면 삭제)
    private final Map<Long, Long> userLocationMap = new ConcurrentHashMap<>();

    //유저가 방에 진입했을 때 기록 (웹소켓 연결이나 특정 이벤트 시점)
    public void enterRoom(Long userId, Long roomId) {
        userLocationMap.put(userId, roomId);
    }

    //유저가 방에서 나갔을 때
    public void leaveRoom(Long userId) {
        userLocationMap.remove(userId);
    }

    //상대방이 지금 이 방에 들어와있는 상태인지 검증
    public boolean isUserInRoom(Long userId, Long roomId) {
        Long currentRoomId = userLocationMap.get(userId);
        return currentRoomId != null && currentRoomId.equals(roomId);
    }
}
