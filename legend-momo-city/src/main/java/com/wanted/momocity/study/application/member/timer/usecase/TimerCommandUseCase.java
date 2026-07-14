package com.wanted.momocity.study.application.member.timer.usecase;

/*
 * comment.
 *  그룹방 내 개인 타이머 쓰기 작업 전용 UseCase 인터페이스
 *  - 시작(재개 포함)/일시정지/종료
 * */

import com.wanted.momocity.study.application.member.timer.result.TimerActionResult;

public interface TimerCommandUseCase {

    // 타이머 시작 (신규 시작 + 일시정지 후 재개 통합, Result의 action 필드로 구분)
    TimerActionResult start(Long userId, Long roomId);

    // 타이머 일시정지
    TimerActionResult pause(Long userId, Long roomId);

    // 타이머 완전 종료 (방은 유지, timerStatus만 null로)
    TimerActionResult end(Long userId, Long roomId);

}
