package com.wanted.momocity.notification.application.usecase;

import com.wanted.momocity.notification.application.command.*;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationCommandUseCase {

    //알림 읽기
    void readNotificationCommandHandle(ReadNotificationCommand command);

    //알림 삭제
    void removeNotificationCommandHandle(RemoveNotificationCommand command);
}
