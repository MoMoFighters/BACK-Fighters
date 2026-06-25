package com.wanted.momocity.order.application.command;

import com.wanted.momocity.order.domain.model.Reason;
import com.wanted.momocity.order.domain.model.Type;

public record MakeOrderCommand(
        Long userId,
        Reason reason,
        String itemName
) {
    public Type type() { return Type.USED; }
}
