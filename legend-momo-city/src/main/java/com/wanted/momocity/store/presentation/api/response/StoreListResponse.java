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
                    /*comment
                    *  이게 상품 갯수만큼 반복 호출됨
                    *  List면 contains()를 호출할 때마다 리스트 맨 앞부터 하나씩 비교해야하지만
                    *  HashSet이면 바로 찾아감 
                    *  */
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