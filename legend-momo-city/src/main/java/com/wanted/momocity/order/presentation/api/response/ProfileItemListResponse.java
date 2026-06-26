package com.wanted.momocity.order.presentation.api.response;

import com.wanted.momocity.order.domain.model.ProfileItemResult;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record ProfileItemListResponse(
        List<Item> owned,
        List<Item> notOwned
) {
    public record Item(
            String itemName,
            String imageUrl
    ) {}

    // List<ProfileItemResult>를 받아서 owned/notOwned로 나눔
    public static ProfileItemListResponse toResponse(List<ProfileItemResult> results) {
        Map<Boolean, List<Item>> partitioned = results.stream()
                .collect(Collectors.partitioningBy(
                        ProfileItemResult::owned,
                        Collectors.mapping(
                                r -> new Item( r.itemName(), r.imageUrl()),
                                Collectors.toList()
                        )
                ));

        /*comment
        *  partitioningBy : boolean 기준으로 무조건 두 그룹으로 나눠줌
        *  Map 에서 boolean값을 키로 둬서 구분이 가능하게 함
        *  partitioningBy 가 owned() 값을 보고 true/false 로 분류 */

        return new ProfileItemListResponse(
                partitioned.get(true), // 나뉜 것 중 owned가 true인 거 -> 소유 중인 거
                partitioned.get(false) // 나뉜 것 중 owned가 false인 거 -> 소유 중이지 않은 거
        );
    }
}
