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
    public List<ProfileItemResult> getAvailableProfile(Long userId) {

        List<StoreItemResult> allItems = getAllProductPort.getAllProfileItems();
        List<Long> ownedItemIds = orderRepository.findOwnedItemIdsByUserIdAndReason(userId, Reason.PROFILE);
//        Set<Long> ownedItemIds = orderRepository.findOwnedItemIdsByUserIdAndReason(userId, Reason.PROFILE);

        // 기본 프로필
        ProfileItemResult defaultProfile = new ProfileItemResult(
                Profile.DEFAULT_PROFILE_ITEM_ID,
                Profile.DEFAULT_PROFILE_ITEM_NAME,
                Profile.DEFAULT_PROFILE_IMAGE_URL,
                true // 기본 프사는 누구나 무조건 owned로
        );

        List<ProfileItemResult> storeProfiles = allItems.stream()
                .map(item -> new ProfileItemResult(
                        item.itemId(),
                        item.itemName(),
                        item.imageUrl(),
                        ownedItemIds.contains(item.itemId())
                ))
                .toList();

        // 기본 프사를 항상 소유중인 목록에 포함해서 응답
        return Stream.concat(Stream.of(defaultProfile), storeProfiles.stream()).toList();
    }
}
