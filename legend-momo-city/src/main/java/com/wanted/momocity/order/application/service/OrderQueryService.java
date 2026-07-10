package com.wanted.momocity.order.application.service;

import com.wanted.momocity.global.domain.profile.Profile;
import com.wanted.momocity.order.application.port.GetAllProductPort;
import com.wanted.momocity.order.application.usecase.OrderQueryUsecase;
import com.wanted.momocity.order.domain.model.*;
import com.wanted.momocity.order.domain.repositroy.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderQueryService implements OrderQueryUsecase {

    private final OrderRepository orderRepository;
    private final GetAllProductPort getAllProductPort;

    @Override
    public OrderHistoryList getOrderHistory(Long userId, int page, int size) {

        List<ListResult> list = orderRepository.getOrderHistory(userId,page,size);

        long totalElements = orderRepository.countByUserId(userId);
        int totalPages = (int) Math.ceil((double) totalElements / size);

        return new OrderHistoryList(list, page, size, totalElements, totalPages);
    }

    // 사용 가능한 프사 목록 조회
    @Override
    public ProfileItemPageResult getAvailableProfile(Long userId, int page, int size) {

        List<ProfileItemResult> sortedItems = getOwnedFirstSortedItems(userId); // 전체 상품 목록에서 가진 게 윗쪽으로 오도록 정렬해서 목록 가져옴
        List<ProfileItemResult> allItemsWithDefault = prependDefaultProfile(sortedItems); // 기본 프사 있을 때

        List<ProfileItemResult> pageItems = paginate(allItemsWithDefault, page, size); // 기본 포함 페이지네이션 1페이지

        long totalElements = allItemsWithDefault.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        return new ProfileItemPageResult(pageItems, page, size, totalElements, totalPages);
    }

    // 전체 프사 상품에 소유 여부를 매기고 owned가 앞에 오도록
    private List<ProfileItemResult> getOwnedFirstSortedItems(Long userId) {
        List<StoreItemResult> allItems = getAllProductPort.getAllProfileItems(); // 전체 상품 목록 가지고옴
        List<Long> ownedItemIds = orderRepository.findOwnedItemIdsByUserIdAndReason(userId, Reason.PROFILE); // 그 중 소유중인 상품들 목록

        return allItems.stream()
                .map(item -> toProfileItemResult(item, ownedItemIds))
                .sorted(Comparator.comparing(ProfileItemResult::owned).reversed())
                .toList();
        /*comment
        *  Comparator<T> comparator = (a, b)
        *  a가 b보다 앞에 와야 하면 음수
        *  a가 b보다 뒤에 와야 하면 양수
        *  같으면 0
        *  boolean 비교는 false < true
        *  이 상태로 정렬하면 false(안 가진 것)가 먼저 true(가진 것)가 나중에라서 reverse 처리 해줌
        */
    }

    private ProfileItemResult toProfileItemResult(StoreItemResult item, List<Long> ownedItemIds) {
        return new ProfileItemResult(
                item.itemId(),
                item.itemName(),
                item.imageUrl(),
                ownedItemIds.contains(item.itemId())
        );
    }

    // 페이지네이션
    private List<ProfileItemResult> paginate(List<ProfileItemResult> items, int page, int size) {
        int fromIndex = Math.min((page - 1) * size, items.size());
        int toIndex = Math.min(fromIndex + size, items.size());
        return items.subList(fromIndex, toIndex);
    }

    // 기본 프로필을 정렬된 목록 맨 앞에
    private List<ProfileItemResult> prependDefaultProfile(List<ProfileItemResult> items) {
        ProfileItemResult defaultProfile = new ProfileItemResult(
                Profile.DEFAULT_PROFILE_ITEM_ID,
                Profile.DEFAULT_PROFILE_ITEM_NAME,
                Profile.DEFAULT_PROFILE_IMAGE_URL,
                true
        );
        return Stream.concat(Stream.of(defaultProfile), items.stream()).toList();
    }

}
