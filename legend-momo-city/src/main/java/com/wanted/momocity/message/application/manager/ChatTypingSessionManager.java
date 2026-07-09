package com.wanted.momocity.message.application.manager;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class ChatTypingSessionManager {
    // key: 방ID, value: 그 방에서 지금 타이핑 중인 유저ID 집합
    private final Map<Long, Set<Long>> typingUsersMap = new ConcurrentHashMap<>();

    //타이핑 시작
    public void startTyping(Long roomId, Long userId) {
        typingUsersMap.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(userId);
    }

    //타이핑 종료
    public void stopTyping(Long roomId, Long userId) {
        Set<Long> users = typingUsersMap.get(roomId);
        if (users != null) {
            users.remove(userId);
            if (users.isEmpty()) {
                typingUsersMap.remove(roomId);
            }
        }
    }

    //연결 종료(DISCONNECT) 시 모든 방에서 이 유저 제거
    public void clearUser(Long userId) {
        typingUsersMap.values().forEach(set -> set.remove(userId));
    }

    //현재 그 방에서 타이핑 중인 유저ID 목록 조회
    public Set<Long> getTypingUsers(Long roomId) {
        return typingUsersMap.getOrDefault(roomId, Set.of());
    }
}
