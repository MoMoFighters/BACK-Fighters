package com.wanted.momocity.user.application.port;

public interface GetItemUrlPort {
    // 품목 이름으로 url 반환하기
    String getItemUrl(String itemName,Long userId);
}
