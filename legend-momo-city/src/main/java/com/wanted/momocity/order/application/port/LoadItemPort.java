package com.wanted.momocity.order.application.port;

import com.wanted.momocity.order.domain.model.CheckItem;

public interface LoadItemPort {

    // 아이템 이름으로 id찾기
    CheckItem findByName(String itemName);
}
