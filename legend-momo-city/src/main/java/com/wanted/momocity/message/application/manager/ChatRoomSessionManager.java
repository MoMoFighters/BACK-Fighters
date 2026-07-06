package com.wanted.momocity.message.application.manager;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class ChatRoomSessionManager {

    //key: 유저ID, value: 현재 머물고 있는 방ID(방에서 나가면 삭제)
    private final Map<Long, Long> userLocationMap = new ConcurrentHashMap<>();

    //유저가 방에 진입했을 때 기록 (웹소켓 연결이나 특정 이벤트 시점)
    public void enterRoom(Long userId, Long roomId) {
        userLocationMap.put(userId, roomId);
    }

    //유저가 방에서 나갔을 때
    public void leaveRoom(Long userId, Long roomId) {
        Long currentRoomId = userLocationMap.get(userId);

        // 현재 장부에 등록된 방 번호가 내가 방금 구독 해제(UNSUBSCRIBE) 요청한 방 번호와 정확히 일치할 때만 삭제!
        if (currentRoomId != null && currentRoomId.equals(roomId)) {
            userLocationMap.remove(userId);
            log.info("[SessionManager] 유저 {}번의 {}번 방 세션 정상 제거 완료", userId, roomId);
        } else {
            // 그 사이에 다른 방(새로 진입한 방)으로 업데이트되었다면 지우지 않고 유지하여 레이스 컨디션 방어
            log.info("[SessionManager] 유저 {}번은 이미 다른 방({})에 진입해 있으므로 {}번 방 제거 요청을 무시합니다.",
                    userId, currentRoomId, roomId);
        }
    }

    // 🎯 2. [DISCONNECT 전용 추가] 연결 종료 시 방 번호 안 따지고 무조건 제거 (인터셉터 에러 해결책)
    public void leaveRoom(Long userId) {
        Long removedRoomId = userLocationMap.remove(userId);
        if (removedRoomId != null) {
            log.info("[SessionManager] 웹소켓 끊김으로 유저 {}번의 {}번 방 세션 강제 청소 완료", userId, removedRoomId);
        }
    }

    //상대방이 지금 이 방에 들어와있는 상태인지 검증
    public boolean isUserInRoom(Long userId, Long roomId) {
        Long currentRoomId = userLocationMap.get(userId);
        return currentRoomId != null && currentRoomId.equals(roomId);
    }
}
