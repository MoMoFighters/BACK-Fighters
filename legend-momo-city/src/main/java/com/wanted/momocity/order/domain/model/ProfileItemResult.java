package com.wanted.momocity.order.domain.model;

public record ProfileItemResult(
        // 전체 상품 목록 + 소유 여부
        Long itemId,
        String itemName,
        String imageUrl,
        boolean owned
) {
}
