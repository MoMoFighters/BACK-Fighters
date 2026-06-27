package com.wanted.momocity.store.presentation.api.response;

import com.wanted.momocity.store.domain.model.Store;
import com.wanted.momocity.store.domain.model.StoreListResult;
import com.wanted.momocity.store.domain.model.Type;

import java.util.List;
import java.util.Set;

public record StoreListResponse(
        List<Product> stores,
        Long point,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public record Product(
            Long id,
            String name,
            Long price,
            String url,
            Type type,
            boolean isOwned
    ) {
        public static Product toResponse(Store store, Set<Long> ownedItemId) {
            return new Product(
                    store.getId(),
                    store.getName(),
                    store.getPrice(),
                    store.getUrl(),
                    store.getType(),
                    ownedItemId.contains(store.getId())
            );
        }
    }

    public static StoreListResponse from(StoreListResult result) {
        List<Product> items = result.stores()
                .stream()
                .map(store -> Product.toResponse(store, result.ownedItemIds()))
                .toList();
        return new StoreListResponse(items, result.point(), result.page(), result.size(), result.totalElements(), result.totalPages());
    }
}