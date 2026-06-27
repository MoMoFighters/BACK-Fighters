package com.wanted.momocity.order.infrastructure.adapter;

import com.wanted.momocity.global.application.point.AddOrderHistory;
import com.wanted.momocity.order.domain.model.Reason;
import com.wanted.momocity.order.domain.model.Type;
import com.wanted.momocity.order.infrastructure.persistence.OrderJpaEntity;
import com.wanted.momocity.order.infrastructure.persistence.SpringDataOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class AddOrderHistoryAdapter implements AddOrderHistory {
    private final SpringDataOrderRepository springDataOrderRepository;

    @Override
    public void saveOrderHistory(Long userId, Reason reason, Type type, Long amount) {
        springDataOrderRepository.save(
                new OrderJpaEntity(userId, reason, type, amount, null, LocalDateTime.now())
        );
    }
}
