package com.wanted.momocity.order.domain.model;

import java.util.List;

public record OrderHistoryList(
        List<ListResult> list,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
