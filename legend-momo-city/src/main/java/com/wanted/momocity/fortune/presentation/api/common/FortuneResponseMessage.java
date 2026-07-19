package com.wanted.momocity.fortune.presentation.api.common;

public final class FortuneResponseMessage {
    // 객체 생성을 막고 상수만 사용하도록 private 생성자를 선언합니다.
    private FortuneResponseMessage() {
    }

    // 오늘의 운세를 정상적으로 반환했을 때 사용하는 메시지입니다.
    public static final String DRAW_SUCCESS = "오늘의 운세를 조회했습니다.";

    // 운세 뽑기에 필요한 포인트가 부족할 때 사용하는 메시지입니다.
    public static final String INSUFFICIENT_POINT = "운세를 뽑기 위한 포인트가 부족합니다.";

    // 운세 원본 데이터를 조회할 수 없을 때 사용하는 메시지입니다.
    public static final String DATA_NOT_FOUND = "운세 데이터를 찾을 수 없습니다.";
}
