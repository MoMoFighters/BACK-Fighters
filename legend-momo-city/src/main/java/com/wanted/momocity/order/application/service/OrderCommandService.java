package com.wanted.momocity.order.application.service;

import com.wanted.momocity.global.application.point.PointChange;
import com.wanted.momocity.order.application.command.MakeOrderCommand;
import com.wanted.momocity.order.application.policy.OrderPolicy;
import com.wanted.momocity.order.application.usecase.OrderCommandUsecase;
import com.wanted.momocity.order.domain.repositroy.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class OrderCommandService implements OrderCommandUsecase {

    private final OrderRepository orderRepository;
    private final OrderPolicy orderPolicy;

    private final PointChange pointChange;

    // 상품 구매
    @Override
    public void makeOrder(MakeOrderCommand makeOrderCommand) {
        orderPolicy.orderPolicy(makeOrderCommand);
        // 포인트 차감
        pointChange.usePoint(makeOrderCommand.userId(), makeOrderCommand.amount());

        orderRepository.makeOrder(makeOrderCommand.userId(),makeOrderCommand.reason(),makeOrderCommand.type(),
                makeOrderCommand.amount(),makeOrderCommand.itemId());
        log.info("[order] 상품 구매 완료 | userId={} | itemId={}", makeOrderCommand.userId(), makeOrderCommand.itemId());

    }
}
