package com.wanted.momocity.order.presentation.api.response;

import com.wanted.momocity.order.domain.model.ListResult;
import com.wanted.momocity.order.domain.model.OrderHistoryList;
import com.wanted.momocity.order.domain.model.Reason;
import com.wanted.momocity.order.domain.model.Type;

import java.time.LocalDateTime;
import java.util.List;

public record OrderHistoryResponse(
        List<OrderHistoryItem> list,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public record OrderHistoryItem(
            Type type,
            Reason reason,
            LocalDateTime createdAt,
            Long amount
    ) {
        public static OrderHistoryItem from(ListResult result) {
            return new OrderHistoryItem(result.type(), result.reason(), result.createdAt(), result.amount());
        }
    }

    public static OrderHistoryResponse toResponse(OrderHistoryList result) {
        return new OrderHistoryResponse(
                result.list().stream().map(OrderHistoryItem::from).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages()
        );
    }
}