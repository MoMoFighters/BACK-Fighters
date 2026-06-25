package com.wanted.momocity.notification.presentation.api.response;

import com.wanted.momocity.message.application.usecase.MessageCommandUseCase.CreateRoomView;

import java.time.LocalDateTime;
import java.util.List;

public record GetNotificationResponse(
        Long notificationId,
        String type,
        String message,
        Boolean isRead,
        Long refId,
        LocalDateTime createdAt
) {
}
