package com.wanted.momocity.notification.presentation.api.request;

import java.util.List;

public record ReadNotificationRequest(
        List<Long> targetId
) {
}
