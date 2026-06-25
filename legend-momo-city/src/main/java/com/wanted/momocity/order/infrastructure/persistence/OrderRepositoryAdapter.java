package com.wanted.momocity.order.infrastructure.persistence;

import com.wanted.momocity.order.domain.exception.AlreadyOwnedException;
import com.wanted.momocity.order.domain.model.Reason;
import com.wanted.momocity.order.domain.model.Type;
import com.wanted.momocity.order.domain.repositroy.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Repository
@Transactional
@RequiredArgsConstructor
public class OrderRepositoryAdapter implements OrderRepository {

    private final SpringDataOrderRepository springDataOrderRepository;

    // 상품 구매
    @Override
    public void makeOrder(Long userId, Reason reason, Type type, Long amount, Long itemId) {

        try {
            OrderJpaEntity entity = new OrderJpaEntity(userId, reason, type, amount, itemId, LocalDateTime.now());
            springDataOrderRepository.save(entity);
        } catch (DataIntegrityViolationException e) {
            throw new AlreadyOwnedException("이미 보유한 상품입니다.");
        }

    }

    // 이미 구매한 상품인지 확인
    @Override
    public boolean existsByUserIdAndItemId(Long userId, Long itemId) {
        return springDataOrderRepository.existsByUserIdAndItemId(userId, itemId);
    }
}
