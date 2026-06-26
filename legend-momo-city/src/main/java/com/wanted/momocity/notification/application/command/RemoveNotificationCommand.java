package com.wanted.momocity.notification.application.command;

import java.util.List;

public record RemoveNotificationCommand(
        Long userId,
        List<Long> targetId
) {
}
