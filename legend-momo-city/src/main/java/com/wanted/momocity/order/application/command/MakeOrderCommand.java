package com.wanted.momocity.order.application.command;

import com.wanted.momocity.order.domain.model.Reason;
import com.wanted.momocity.order.domain.model.Type;

public record MakeOrderCommand(
        Long userId,
        Reason reason,
        Long itemId,
        Long amount
) {
    public Type type() { return Type.USED; }
}
