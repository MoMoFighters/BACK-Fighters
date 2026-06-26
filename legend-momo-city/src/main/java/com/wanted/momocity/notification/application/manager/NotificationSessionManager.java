package com.wanted.momocity.notification.application.manager;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class NotificationSessionManager {
    // Key: 유저ID, Value: 구독 상태 플래그 (구독 중이면 true)
    private final Map<Long, Boolean> activeNotificationUsers = new ConcurrentHashMap<>();

    // 유저가 전체 알림 개수 채널을 구독했을 때
    public void enterNotificationChannel(Long userId) {
        if (userId != null) {
            activeNotificationUsers.put(userId, true);
        }
    }

    // 알림 채널 구독을 취소하거나 연결이 끊겼을 때
    public void leaveNotificationChannel(Long userId) {
        if (userId != null) {
            activeNotificationUsers.remove(userId);
        }
    }

    // 현재 이 유저가 알림 채널을 활성화(구독)하고 있는지 검증
    public boolean isUserSubscribed(Long userId) {
        return activeNotificationUsers.getOrDefault(userId, false);
    }
}
