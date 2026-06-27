package com.wanted.momocity.store.domain.model;

import java.util.List;

public record StoreListResult(

        List<Store> stores,
        Long point,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
