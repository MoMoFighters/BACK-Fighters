package com.wanted.momocity.study.application.common.usecase;

/*
 * comment.
 *  타이머 시작 가능 여부 조회 전용 UseCase 인터페이스
 *  -
 *  해당 API는 solo나 room 어느 한쪽 소유가 아닌 유저 전역 상태를 판단
 *  솔로 화면 진입 시에도, 그룹방 화면 진입 시에도 동일하게 이 API 하나를 호출
 * */

import com.wanted.momocity.study.presentation.api.response.common.TimerAvailabilityResponse;

public interface TimerAvailabilityUseCase {

    // 로그인한 유저가 지금 새로운 타이머를 시작할 수 있는 상태인지 조회
    TimerAvailabilityResponse getAvailability(Long userId);

}
