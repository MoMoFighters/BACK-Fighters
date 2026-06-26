package com.wanted.momocity.order.application.service;

import com.wanted.momocity.order.application.usecase.OrderQueryUsecase;
import com.wanted.momocity.order.domain.model.ListResult;
import com.wanted.momocity.order.domain.model.OrderHistoryList;
import com.wanted.momocity.order.domain.repositroy.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderQueryService implements OrderQueryUsecase {

    private final OrderRepository orderRepository;

    @Override
    public OrderHistoryList getOrderHistory(Long userId, int page, int size) {

        List<ListResult> list = orderRepository.getOrderHistory(userId,page,size);
        long totalElements = orderRepository.countByUserId(userId);
        int totalPages = (int) Math.ceil((double) totalElements / size);

        return new OrderHistoryList(list, page, size, totalElements, totalPages);
    }
}
