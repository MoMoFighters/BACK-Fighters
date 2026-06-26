package com.wanted.momocity.order.application.service;

import com.wanted.momocity.order.application.port.GetAllProductPort;
import com.wanted.momocity.order.application.usecase.OrderQueryUsecase;
import com.wanted.momocity.order.domain.model.*;
import com.wanted.momocity.order.domain.repositroy.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderQueryService implements OrderQueryUsecase {

    private final OrderRepository orderRepository;
    private final GetAllProductPort getAllProductPort;

    @Override
    public OrderHistoryList getOrderHistory(Long userId, int page, int size) {

        List<ListResult> list = orderRepository.getOrderHistory(userId,page,size);
        long totalElements = orderRepository.countByUserId(userId);
        int totalPages = (int) Math.ceil((double) totalElements / size);

        return new OrderHistoryList(list, page, size, totalElements, totalPages);
    }

    // 사용 가능한 프사 목록 조회
    @Override
    public List<ProfileItemResult> getAvailableProfile(Long userId) {

        List<StoreItemResult> allItems = getAllProductPort.getAllProfileItems();
        List<Long> ownedItemIds = orderRepository.findOwnedItemIdsByUserIdAndReason(userId, Reason.PROFILE);
//        Set<Long> ownedItemIds = orderRepository.findOwnedItemIdsByUserIdAndReason(userId, Reason.PROFILE);


        return allItems.stream()
                .map(item -> new ProfileItemResult(
                        item.itemId(),
                        item.itemName(),
                        item.imageUrl(),
                        ownedItemIds.contains(item.itemId())
                ))
                .toList();
    }
}
