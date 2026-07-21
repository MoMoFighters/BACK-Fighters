package com.wanted.momocity.notification.presentation.api.response;

import com.wanted.momocity.notification.application.usecase.NotificationQueryUseCase.NotiView;

import java.time.LocalDateTime;

public record GetNotificationResponse(
        Long notificationId,
        String type,
        String message,
        Boolean isRead,
        Long refId,
        LocalDateTime createdAt
) {
    // 컨트롤러 스트림 링킹을 위한 정적 팩토리 메서드 추가
    public static GetNotificationResponse from(NotiView view) {
        return new GetNotificationResponse(
                view.notificationId(),
                view.type(),
                view.message(),
                view.isRead(),
                view.refId(),
                view.createdAt()
        );
    }
}
