package com.wanted.momocity.store.domain.model;

import java.util.List;
import java.util.Set;

public record StoreListResult(

        List<Store> stores,
        Long point,
        int page,
        int size,
        long totalElements,
        int totalPages,
        Set<Long> ownedItemIds
) {
}
