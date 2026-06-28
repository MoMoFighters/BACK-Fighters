package com.wanted.momocity.user.domain.model;

import java.time.LocalDateTime;

public record CheckStatusResult(
        // suspensionCount 에 따라 status랑 suspendedUntil 을 언제까지로 할지
        Status status,
        LocalDateTime suspendedUntil
) {
}