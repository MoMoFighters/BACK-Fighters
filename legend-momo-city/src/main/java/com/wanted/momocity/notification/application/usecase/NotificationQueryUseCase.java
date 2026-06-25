package com.wanted.momocity.notification.application.usecase;

import com.wanted.momocity.notification.application.query.GetNotificationQuery;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NotificationQueryUseCase {

    //알림 목록
    List<NotiView> getNotificationQueryHandle(GetNotificationQuery query);

    record NotiView(
            Long notificationId,
            String type,
            String message,
            Boolean isRead,
            Long refId,
            LocalDateTime createdAt
    ) {}
}
