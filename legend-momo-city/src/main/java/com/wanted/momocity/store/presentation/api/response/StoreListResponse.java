package com.wanted.momocity.store.presentation.api.response;

import com.wanted.momocity.store.domain.model.Store;
import com.wanted.momocity.store.domain.model.StoreListResult;
import com.wanted.momocity.store.domain.model.Type;

import java.util.List;

public record StoreListResponse(
        List<Product> stores,
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
            Type type
    ) {
        public static Product toResponse(Store store) {
            return new Product(
                    store.getId(),
                    store.getName(),
                    store.getPrice(),
                    store.getUrl(),
                    store.getType()
            );
        }
    }

    public static StoreListResponse from(StoreListResult result) {
        List<Product> items = result.stores()
                .stream()
                .map(Product::toResponse)
                .toList();
        return new StoreListResponse(items, result.page(), result.size(), result.totalElements(), result.totalPages());
    }
}