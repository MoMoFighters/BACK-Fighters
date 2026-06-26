package com.wanted.momocity.notification.presentation.api.request;

import java.util.List;

public record RemoveNotificationRequest(
        List<Long> targetId
) {
}
