package com.wanted.momocity.order.application.policy;

import com.wanted.momocity.order.application.command.MakeOrderCommand;
import com.wanted.momocity.order.application.port.CheckPointPort;
import com.wanted.momocity.order.application.port.LoadItemPort;
import com.wanted.momocity.order.domain.exception.AlreadyOwnedException;
import com.wanted.momocity.order.domain.exception.InsufficientPointException;
import com.wanted.momocity.order.domain.repositroy.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderPolicy {

    private final CheckPointPort checkPointPort;
    private final OrderRepository orderRepository;

    public void orderPolicy(MakeOrderCommand command, Long itemId, Long price) {
        if (orderRepository.existsByUserIdAndItemId(command.userId(),itemId)) {
            throw new AlreadyOwnedException("이미 보유한 상품입니다.");
        }
        if (!checkPointPort.isPointAble(command.userId(), price)) {
            throw new InsufficientPointException("포인트가 부족합니다.");
        }
    }
}
