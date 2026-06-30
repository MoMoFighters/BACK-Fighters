package com.wanted.momocity.notification.infrastructure.event;

public record NotificationCreatedPublishedEvent(
        Long userId,
        String type //ALL(알림 3개 다), NOTPHONE(폰 제외 알림)
) {
}
