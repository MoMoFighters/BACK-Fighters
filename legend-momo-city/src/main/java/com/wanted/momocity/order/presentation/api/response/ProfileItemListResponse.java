package com.wanted.momocity.order.presentation.api.response;

import com.wanted.momocity.order.domain.model.ProfileItemPageResult;
import com.wanted.momocity.order.domain.model.ProfileItemResult;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record ProfileItemListResponse(
        List<Item> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public record Item(
            String itemName,
            String imageUrl,
            boolean isOwned
    ) {}

    public static ProfileItemListResponse toResponse(ProfileItemPageResult result) {
        List<Item> items = result.items().stream()
                .map(r -> new Item(r.itemName(), r.imageUrl(), r.owned()))
                .toList();

        return new ProfileItemListResponse(
                items,
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages()
        );
    }
}
