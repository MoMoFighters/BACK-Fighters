package com.wanted.momocity.notification.application.manager;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class NotificationSessionManager {
    // Key: 유저ID, Value: 구독 상태 플래그 (구독 중이면 true)
    private final Map<Long, Set<String>> activeNotificationUsers = new ConcurrentHashMap<>();

    // 유저가 전체 알림 개수 채널을 구독했을 때
    public void enterNotificationChannel(Long userId, String sessionId) {
        if (userId != null && sessionId != null) {
            // 해당 유저의 Set이 없으면 새로 만들고, 있으면 기존 Set에 세션 ID를 추가
            activeNotificationUsers.computeIfAbsent(userId, k -> new CopyOnWriteArraySet<>())
                    .add(sessionId);
        }
    }

    // 알림 채널 구독을 취소하거나 연결이 끊겼을 때
    public void leaveNotificationChannel(Long userId, String sessionId) {
        if (userId != null && sessionId != null) {
            activeNotificationUsers.computeIfPresent(userId, (key, sessionSet) -> {
                // 해당 세션 제거
                sessionSet.remove(sessionId);

                // 제거 후 더 이상 남아있는 세션(탭)이 없다면 Map에서 유저 자체를 삭제(null 반환)
                return sessionSet.isEmpty() ? null : sessionSet;
            });
        }
    }

    // 현재 이 유저가 알림 채널을 활성화(구독)하고 있는지 검증
    public boolean isUserSubscribed(Long userId) {
        Set<String> sessions = activeNotificationUsers.get(userId);
        // 세션 셋이 존재하고, 그 안에 활성화된 세션이 1개라도 있으면 온라인(true)
        return sessions != null && !sessions.isEmpty();    }
}
