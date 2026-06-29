package com.wanted.momocity.notification.application.command;

import java.util.List;

public record ReadNotificationCommand(
        Long userId, //로그인 유저
        List<Long> targetId //읽을 알림들
) {
}
