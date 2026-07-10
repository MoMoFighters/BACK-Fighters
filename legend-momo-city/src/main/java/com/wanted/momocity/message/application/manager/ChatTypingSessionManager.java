package com.wanted.momocity.message.application.manager;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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
        typingUsersMap.computeIfPresent(roomId, (k, users) -> {
            users.remove(userId);
            return users.isEmpty() ? null : users;
        });
    }

    //연결 종료(DISCONNECT) 시 모든 방에서 이 유저 제거
    public void clearUser(Long userId) {
        typingUsersMap.entrySet().removeIf(entry -> {
            entry.getValue().remove(userId);
            return entry.getValue().isEmpty();
        });
    }

    //현재 그 방에서 타이핑 중인 유저ID 목록 조회
    public Set<Long> getTypingUsers(Long roomId) {
        return typingUsersMap.getOrDefault(roomId, Set.of());
    }

    // 추가: 이 유저가 지금 타이핑 중인 모든 방ID 조회
    public Set<Long> getTypingRooms(Long userId) {
        return typingUsersMap.entrySet().stream()
                .filter(entry -> entry.getValue().contains(userId))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }
}
