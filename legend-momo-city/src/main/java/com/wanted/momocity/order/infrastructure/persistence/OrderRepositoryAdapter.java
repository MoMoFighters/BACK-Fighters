package com.wanted.momocity.order.infrastructure.persistence;

import com.wanted.momocity.order.domain.exception.AlreadyOwnedException;
import com.wanted.momocity.order.domain.model.ListResult;
import com.wanted.momocity.order.domain.model.Reason;
import com.wanted.momocity.order.domain.model.Type;
import com.wanted.momocity.order.domain.repositroy.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    // 포인트 내역 조회
    @Override
    public List<ListResult> getOrderHistory(Long userId, int page, int size) {
        return springDataOrderRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page - 1, size))
                .stream()
                .map(OrderJpaEntity::toDomain)
                .toList();
    }

    // 페이지네이션
    @Override
    public long countByUserId(Long userId) {
        return springDataOrderRepository.countByUserId(userId);
    }

    // 소유한 프사 목록 조회
    @Override
    public List<Long> findOwnedItemIdsByUserIdAndReason(Long userId, Reason reason) {
        return springDataOrderRepository.findOwnedItemIdsByUserIdAndReason(userId, reason);
    }
//    @Override
//    public Set<Long> findOwnedItemIdsByUserIdAndReason(Long userId, Reason reason) {
//        return new HashSet<>(springDataOrderRepository.findOwnedItemIdsByUserIdAndReason(userId, reason));
//    }
}
