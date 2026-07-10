package com.wanted.momocity.order.domain.model;

import java.util.List;

public record ProfileItemPageResult(
        List<ProfileItemResult> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
