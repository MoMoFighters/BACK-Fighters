package com.wanted.momocity.order.domain.model;

import java.time.LocalDateTime;

public record ListResult(
        Type type,
        Reason reason,
        LocalDateTime createdAt,
        Long amount
) {
}
