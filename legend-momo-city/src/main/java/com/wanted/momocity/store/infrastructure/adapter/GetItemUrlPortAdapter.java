package com.wanted.momocity.store.infrastructure.adapter;

import com.wanted.momocity.global.domain.profile.Profile;
import com.wanted.momocity.store.application.port.CheckIsOrderedPort;
import com.wanted.momocity.store.domain.exception.ItemNotFoundException;
import com.wanted.momocity.store.domain.exception.ItemNotOwnedException;
import com.wanted.momocity.store.domain.model.CheckIsOrderedResult;
import com.wanted.momocity.store.infrastructure.persistence.SpringDataStoreRepository;
import com.wanted.momocity.user.application.port.GetItemUrlPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetItemUrlPortAdapter implements GetItemUrlPort {

    private final SpringDataStoreRepository springDataStoreRepository;
    private final CheckIsOrderedPort checkIsOrderedPort;


    // 품목 이름으로 그 url 가져오기
    @Override
    public String getItemUrl(String itemName, Long userId) {

        // 기본 프사는 store 상품이 아니므로 소유 확인 없이 바로 반환
        if (Profile.DEFAULT_PROFILE_ITEM_NAME.equals(itemName)) {
            return Profile.DEFAULT_PROFILE_IMAGE_URL;
        }

        // 실제 존재하는 상품인지 확인
        CheckIsOrderedResult idAndName = springDataStoreRepository.findIdAndUrlByName(itemName)
                .orElseThrow(() -> new ItemNotFoundException("존재하지 않는 상품입니다 : " +itemName));

        // 그 상품을 해당 사용자가 구매한 게 맞는지 확인
        boolean isOrdered = checkIsOrderedPort.checkIsOrdered(idAndName.id(),userId);

        if(isOrdered){ // 맞으면 url 전달
            return idAndName.url();
        }else{ // 아니면 예외처리
            throw new ItemNotOwnedException("보유하지 않은 아이템입니다.");
        }
    }
}
