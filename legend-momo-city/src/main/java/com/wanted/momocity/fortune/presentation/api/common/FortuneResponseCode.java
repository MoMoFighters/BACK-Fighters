package com.wanted.momocity.fortune.presentation.api.common;

// 운세 APi에서 사용할 응답 코드 패키지
/*
*  이 클래스를 사용하는 이유 프론트가 문자열이 아닌 code 기준으로 상황을 정확하게 구분할 수 있게 하기 위해
* */
public final class FortuneResponseCode {
    // 객체 생성을 막고 상수만 사용하도록 private 생성자를 선언합니다.
    private FortuneResponseCode() {
    }

    // 오늘의 운세 조회 및 뽑기가 성공했을 때 사용하는 코드입니다.
    public static final String DRAW_SUCCESS = "FORTUNE-DRAW-SUCCESS";

    // 운세를 뽑기 위한 포인트가 부족할 때 사용하는 코드입니다.
    public static final String INSUFFICIENT_POINT = "FORTUNE-INSUFFICIENT-POINT";

    // 조회할 운세 원본 데이터가 존재하지 않을 때 사용하는 코드입니다.
    public static final String DATA_NOT_FOUND = "FORTUNE-DATA-NOT-FOUND";
}
